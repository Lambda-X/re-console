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

(defn display-console-output-item
  ([console-key value]
   (display-console-output-item console-key value false))
  ([console-key value error?]
   [:div
    {:on-click #(dispatch [:focus-console-editor console-key])
     :class (str "re-console-item" (when error? " re-console-item-error"))}
    value]))

(defn display-console-item
  [console-key to-str-fn item]
  (if-let [text (:text item)]
    [:div.re-console-item {:on-click #(do (dispatch [:console-set-text console-key text])
                                          (dispatch [:focus-console-editor console-key]))}
     [utils/colored-text (str (:ns item) "=> " text)]]
    (display-console-output-item console-key
                                 (to-str-fn (:value item))
                                 (= :error (:type item)))))

(defn console-items
  ([console-key items]
   (into [:div] (map (partial display-console-item console-key identity) items)))
  ([console-key items to-str-fn]
   (into [:div] (map (partial display-console-item console-key to-str-fn) items))))

(defn console [console-key eval-opts]
  (let [items (subscribe [:get-console-items console-key])
        text  (subscribe [:get-console-current-text console-key])]
    (dispatch-sync [:init-console console-key eval-opts])
    (reagent/create-class
     {:reagent-render
      (fn []
        [:div.re-console-container
         {:on-click #(dispatch [:focus-console-editor console-key])}
         [:div.re-console
          [console-items console-key @items (:to-str-fn eval-opts)]
          [editor/console-editor console-key text]]])
      :component-did-update
      (fn [this]
        (common/scroll-to-el-bottom! (reagent/dom-node this)))})))
