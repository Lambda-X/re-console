(ns re-console.example
  (:require [re-console.replumb-proxy :as replumb-proxy]
            [re-console.core :as console]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as reagent]))

(enable-console-print!)

(defn toggle-verbose []
  (let [verbose? (atom false)]
   [:input {:type "button"
            :value "Toggle verbose"
            :on-click #(do (swap! verbose? not)
                           (dispatch [:set-console-eval-opts :cljs-console
                                      (replumb-proxy/eval-opts @verbose? ["/js/compiled/out"])]))}]))

(defn ^:export main []
  (reagent/render [console/console :cljs-console (replumb-proxy/eval-opts false ["/js/compiled/out"])]
                  (.getElementById js/document "app"))
  (reagent/render [toggle-verbose]
                  (.getElementById js/document "buttons")))
