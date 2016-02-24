(ns re-console.example
  (:require [re-console.replumb-proxy :as replumb-proxy]
            [re-console.core :as console]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as reagent]))

(defonce console-key :cljs-console)

(defn toggle-verbose []
  (let [verbose? (subscribe [:get-console-verbose console-key])]
    (fn []
      [:div.buttons-container
       [:input.buttons-element
        {:type "button"
         :value "Toggle verbose"
         :on-click #(do (dispatch [:toggle-verbose console-key])
                        (dispatch [:set-console-eval-opts console-key
                                               (replumb-proxy/eval-opts (not @verbose?) ["/js/compiled/out"])]))}]
       [:span.buttons-element
        "Now is " [:strong (if (false? @verbose?) "false" "true")]]
       [:div
        [:span.buttons-element "Theme"]
        [:select {:on-change (fn [evt]
                               (let [theme (-> evt .-target .-value)]
                                 (dispatch [:set-console-theme console-key theme])))}
         [:option "default"]
         [:option "elegant"]
         [:option "erlang-dark"]]]])))

(defn ^:export main []
  (enable-console-print!)
  (dispatch [:init-verbose console-key])
  (reagent/render [console/console console-key (replumb-proxy/eval-opts false ["/js/compiled/out"])]
                  (.getElementById js/document "app"))
  (reagent/render [toggle-verbose]
                  (.getElementById js/document "buttons")))
