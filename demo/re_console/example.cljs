(ns re-console.example
  (:require [re-console.replumb-proxy :as replumb-proxy]
            [re-console.core :as console]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as reagent]
            [cljsjs.codemirror]
            [cljsjs.codemirror.addon.edit.matchbrackets]
            [cljsjs.codemirror.addon.runmode.runmode]
            [cljsjs.codemirror.addon.runmode.colorize]
            [cljsjs.codemirror.mode.clojure]
            [re-console.parinfer :as parinfer]
            [parinfer.codemirror.mode.clojure.clojure-parinfer]))

(defonce console-key :cljs-console)
(defonce src-paths ["/cljs-src" "/js/compiled/out"])

(defn toggle-verbose []
  (let [verbose? (subscribe [:get-console-verbose])]
    (fn []
      [:div
       [:input.button-element
        {:type "button"
         :value "Toggle verbose"
         :on-click #(do (dispatch [:toggle-verbose])
                        (dispatch [:set-console-eval-opts console-key
                                   (replumb-proxy/eval-opts (not @verbose?) src-paths)]))}]
       [:span
        "Now is " [:strong (if (false? @verbose?) "false" "true")]]])))

(defn toggle-parinfer []
  (let [mode (subscribe [:get-console-mode console-key])]
    (fn []
      [:div
       [:input.button-element
        {:type "button"
         :value "Toggle parinfer"
         :on-click #(let [new-mode (if (= @mode :none) :indent-mode :none)]
                      (dispatch [:set-console-mode console-key new-mode]))}]
       [:span
        "Now is " [:strong (name @mode)]]])))

(defn buttons
  []
  [:div.buttons-container
   [toggle-verbose]
   [toggle-parinfer]])

(defn ^:export main []
  (enable-console-print!)
  (dispatch [:init-options])
  (reagent/render [console/console console-key {:eval-opts (replumb-proxy/eval-opts false src-paths)
                                                :mode-line? true}]
                  (.getElementById js/document "app"))
  (reagent/render [buttons]
                  (.getElementById js/document "buttons")))
