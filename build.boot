(set-env!
 :resource-paths #{"html" "demo" "src" "sources/"}
 :dependencies '[[adzerk/boot-cljs                    "1.7.228-1"      :scope "test"]
                 [pandeiro/boot-http                  "0.7.1-SNAPSHOT" :scope "test"]
                 [adzerk/boot-reload                  "0.4.5"          :scope "test"]
                 [degree9/boot-semver                 "1.2.4"          :scope "test"]
                 [adzerk/boot-cljs-repl               "0.3.0"          :scope "test"]
                 [com.cemerick/piggieback             "0.2.1"          :scope "test"]
                 [weasel                              "0.7.0"          :scope "test"]
                 [org.clojure/tools.nrepl             "0.2.12"         :scope "test"]
                 [crisptrutski/boot-cljs-test         "0.2.2-SNAPSHOT" :scope "test"]
                 [org.clojars.stumitchell/clairvoyant "0.1.0-SNAPSHOT" :scope "test"]
                 [day8/re-frame-tracer                "0.1.0-SNAPSHOT" :scope "test"]
                 [deraen/boot-sass                    "0.2.1"          :scope "test"]
                 [adzerk/bootlaces                    "0.1.13"         :scope "test"]
                 [replumb/boot-pack-source    "0.1.2-1"   :scope "test"]
                 [org.clojure/clojure         "1.7.0"]
                 [org.clojure/clojurescript   "1.7.228"]
                 [reagent                     "0.5.0"]
                 [re-frame                    "0.5.0"]
                 [replumb/replumb             "0.2.2"]
                 [cljsjs/codemirror           "5.10.0-0"]
                 [parinfer                    "0.2.3"]])

;;; clojurescript sources

(def clojurescript-dep '[org.clojure/clojurescript "1.8.40"])
(def pack-source-deps (conj '[[replumb/replumb             "0.2.2-SNAPSHOT"]
                              [org.clojure/tools.reader    "1.0.0-alpha3"]]
                            clojurescript-dep))

(require '[adzerk.boot-cljs             :refer [cljs]]
         '[adzerk.boot-cljs-repl        :refer [cljs-repl start-repl]]
         '[adzerk.boot-reload           :refer [reload]]
         '[crisptrutski.boot-cljs-test  :refer [test-cljs]]
         '[pandeiro.boot-http           :refer [serve]]
         '[deraen.boot-sass             :refer [sass]]
         '[replumb.boot-pack-source     :refer [pack-source]]
         '[boot-semver.core             :refer :all]
         '[adzerk.bootlaces             :refer :all])


(def +version+ (get-version))

(bootlaces! +version+)

(task-options! pom {:project 're-console
                    :version +version+
                    :url "https://github.com/Lambda-X/re-console"
                    :description "A reactive console based on re-frame"}
               test-cljs {:js-env :phantom
                          :out-file "phantom-tests.js"})

;; This prevents a name collision WARNING between the test task and
;; clojure.core/test, a function that nobody really uses or cares
;; about.
(ns-unmap 'boot.user 'test)

(deftask version-file
  "A task that includes the version.properties file in the fileset."
  []
  (with-pre-wrap [fileset]
    (boot.util/info "Add version.properties...\n")
    (-> fileset
        (add-resource (java.io.File. ".") :include #{#"^version\.properties$"})
        commit!)))

(deftask add-cljs-source
  []
  (comp (with-pre-wrap [fs]
          (boot.util/info "Pack source files...\n")
          fs)
        (pack-source :to-dir "cljs-src"
                     :deps (into #{} pack-source-deps)
                     :exclusions '#{org.clojure/clojure
                                    org.mozilla/rhino})))

(deftask test []
  (merge-env! :source-paths #{"test"})
  (comp (speak)
        (test-cljs)))

(deftask auto-test []
  (merge-env! :source-paths #{"test"})
  (comp (watch)
        (test)))

(def foreign-libs
  [{:file "html/js/clojure-parinfer.js"
    :provides ["parinfer.codemirror.mode.clojure.clojure-parinfer"]}])

(deftask dev []
  (comp (version-file)
        (watch)
        (cljs-repl)
        (reload :on-jsload 're-console.example/main)
        (sass)
        (cljs :optimizations :none
              :source-map true
              :compiler-options {:source-map-timestamp true
                                 :foreign-libs foreign-libs})
        (add-cljs-source)
        (serve)))

(deftask build []
  (merge-env! :source-paths #{"src" "demo"} :resource-paths #{"html"})
  (comp (version-file)
        (sass)
        (cljs :optimizations :advanced
              :compiler-options {:foreign-libs foreign-libs})
        (add-cljs-source)))
