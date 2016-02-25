(ns re-console.example
  (:require [re-console.replumb-proxy :as replumb-proxy]
            [re-console.core :as console]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as reagent]))

(defonce console-key :cljs-console)

(defonce example-style {:font-size "15px"
                        :font-family "Menlo, Monaco, Consolas, monospace"
                        :error-msg-color "#e60000"
                        :background-color "white"
                        :height "400"
                        :padding "10"})

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
        "Now is " [:strong (if (false? @verbose?) "false" "true")]]])))

(defn ^:export main []
  (enable-console-print!)
  (dispatch [:init-verbose console-key])
  (reagent/render [console/console console-key example-style (replumb-proxy/eval-opts false ["/js/compiled/out"])]
                  (.getElementById js/document "app"))
  (reagent/render [toggle-verbose]
                  (.getElementById js/document "buttons")))
