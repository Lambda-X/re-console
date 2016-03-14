(ns re-console.replumb-proxy
  (:require [replumb.core :as replumb]
            [replumb.repl :as replumb-repl]
            [re-console.io :as io]))

(defn replumb-options
  [verbose? src-paths]
  (merge (replumb/browser-options src-paths io/fetch-file!)
         {:warning-as-error true
          :verbose verbose?}))

(defn read-eval-call [opts cb source]
  (let [ns (replumb-repl/current-ns)]
    (replumb/read-eval-call opts
                            #(cb {:success? (replumb/success? %)
                                  :result   (replumb/unwrap-result %)
                                  :prev-ns  ns
                                  :source   source})
                            source)))

(defn multiline?
  [input]
  (try
    (replumb-repl/repl-read-string input)
    false
    (catch :default _
      true)))

(defn eval-opts
  [verbose src-path]
  {:get-prompt  replumb/get-prompt
   :should-eval (complement multiline?)
   :evaluate    (partial read-eval-call
                         (replumb-options verbose src-path))})
