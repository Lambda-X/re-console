(def +version+ "0.1.0")

(set-env!
 :resource-paths #{"src" "demo" "html"}
 :dependencies '[[adzerk/boot-cljs            "1.7.228-1"      :scope "test"]
                 [pandeiro/boot-http          "0.7.1-SNAPSHOT" :scope "test"]
                 [adzerk/boot-reload          "0.4.5"          :scope "test"]
                 [adzerk/boot-cljs-repl       "0.3.0"          :scope "test"]
                 [com.cemerick/piggieback     "0.2.1"          :scope "test"]
                 [weasel                      "0.7.0"          :scope "test"]
                 [org.clojure/tools.nrepl     "0.2.12"         :scope "test"]
                 [crisptrutski/boot-cljs-test "0.2.2-SNAPSHOT" :scope "test"]
                 [org.clojure/clojure         "1.7.0"]
                 [org.clojure/clojurescript   "1.7.228"]
                 [reagent                     "0.5.0"]
                 [re-frame                    "0.5.0"]
                 [replumb/replumb             "0.1.3-SNAPSHOT"]
                 [cljsjs/codemirror           "5.10.0-0"]
                 [org.clojars.stumitchell/clairvoyant "0.1.0-SNAPSHOT" :scope "test"]
                 [day8/re-frame-tracer "0.1.0-SNAPSHOT" :scope "test"]])

(require
  '[adzerk.boot-cljs      :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
  '[adzerk.boot-reload    :refer [reload]]
  '[crisptrutski.boot-cljs-test  :refer [test-cljs]]
  '[pandeiro.boot-http    :refer [serve]])

(task-options! pom {:project 'reactive-console
                    :version +version+}
               jar {})

(deftask auto-test []
  (merge-env! :resource-paths #{"test"})
  (comp (watch)
        (speak)
        (test-cljs)))

(deftask dev []
  (comp (serve)
        (watch)
        (speak)
        (cljs-repl)
        (cljs :source-map true :optimizations :none)
        (target)
        (reload :on-jsload 'reactive-console.example/main)))

(deftask build []
  (cljs :optimizations :advanced))
