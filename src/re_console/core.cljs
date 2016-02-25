(ns re-console.core
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [re-console.handlers :as handlers]
            [re-console.subs :as subs]
            [re-console.editor :as editor]
            [re-console.common :as common]
            [re-console.utils :as utils]))

;;; many parts are taken from jaredly's reepl
;;; https://github.com/jaredly/reepl

(defn display-output-item
  ([console-key style value]
   (display-output-item console-key style value false))
  ([console-key style value error?]
   [:div
    {:on-click #(dispatch [:focus-console-editor console-key])
     :style (merge(select-keys style [:font-size :font-family :opacity])
                   (when error? {:color (:error-msg-color style)}))}
    value]))

(defn display-repl-item
  [console-key style item]
  (if-let [text (:text item)]
    [:div.re-console-item
     {:on-click #(do (dispatch [:console-set-text console-key text])
                     (dispatch [:focus-console-editor console-key]))}
     [utils/colored-text (str (:ns item) "=> " text) (select-keys style [:font-size :font-family :opacity])]]
    (if (= :error (:type item))
      (display-output-item console-key style (.-message (:value item)) true)
      (display-output-item console-key style (:value item)))))

(defn repl-items [console-key style items]
  (into [:div] (map #(display-repl-item console-key style %) items)))

(defn console [console-key style eval-opts]
  (let [items (subscribe [:get-console-items console-key])
        text  (subscribe [:get-console-current-text console-key])]
    (dispatch-sync [:init-console console-key eval-opts])
    (reagent/create-class
     {:reagent-render
      (fn []
        [:div.re-console-container
         {:on-click #(dispatch [:focus-console-editor console-key])
          :style (merge {:overflow-y "scroll"}
                        (select-keys style [:background-color :height :padding]))}
         [:div.re-console
          [repl-items console-key style @items]
          [editor/editor console-key style text]]])
      :component-did-update
      (fn [this]
        (common/scroll-to-el-bottom! (reagent/dom-node this)))})))
