(ns reactive-console.example
  (:require [reactive-console.replumb-proxy :as replumb-proxy]
            [reactive-console.core :as rr]
            [reagent.core :as reagent]))

(defonce console-key :cljs-console)
(enable-console-print!)

(defn ^:export main []

  (reagent/render [rr/console console-key replumb-proxy/eval-opts] (.getElementById js/document "app"))

  ;; (let [c (.. js/document (createElement "div"))]
  ;;   (println "Initializing...")
  ;;   (aset c "innerHTML" "<p>i'm dynamically -  created</p>")
  ;;   (.. js/document (getElementById "app") (appendChild c)))
  )
