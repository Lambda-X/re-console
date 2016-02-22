(ns re-console.example
  (:require [re-console.replumb-proxy :as replumb-proxy]
            [re-console.core :as console]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as reagent]))

(defn toggle-verbose []
  (let [verbose? (reagent/atom false)]
    (fn []
      [:div
       [:input {:type "button"
                     :value "Toggle verbose"
                     :on-click #(do (swap! verbose? not)
                                    (dispatch [:set-console-eval-opts :cljs-console
                                               (replumb-proxy/eval-opts @verbose? ["/js/compiled/out"])]))}]
       [:span "Now is " (if (false? @verbose?) "false" "true")]])))

(defn ^:export main []
  (enable-console-print!)
  (reagent/render [console/console :cljs-console (replumb-proxy/eval-opts false ["/js/compiled/out"])]
                  (.getElementById js/document "app"))
  (reagent/render [toggle-verbose]
                  (.getElementById js/document "buttons")))
