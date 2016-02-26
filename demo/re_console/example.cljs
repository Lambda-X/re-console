(ns re-console.example
  (:require [re-console.replumb-proxy :as replumb-proxy]
            [re-console.core :as console]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as reagent]))

(defonce console-key :cljs-console)

(defn toggle-verbose []
  (let [verbose? (subscribe [:get-console-verbose])]
    (fn []
      [:div.buttons-container
       [:input.buttons-element
        {:type "button"
         :value "Toggle verbose"
         :on-click #(do (dispatch [:toggle-verbose])
                        (dispatch [:set-console-eval-opts console-key
                                               (replumb-proxy/eval-opts (not @verbose?) ["/js/compiled/out"])]))}]
       [:span.buttons-element
        "Now is " [:strong (if (false? @verbose?) "false" "true")]]])))

(defn ^:export main []
  (enable-console-print!)
  (dispatch [:init-options])
  (reagent/render [console/console console-key (replumb-proxy/eval-opts false ["/js/compiled/out"])]
                  (.getElementById js/document "app"))
  (reagent/render [toggle-verbose]
                  (.getElementById js/document "buttons")))
