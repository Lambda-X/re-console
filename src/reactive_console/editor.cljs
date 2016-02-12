(ns reactive-console.editor
  (:require [clojure.string :as str]
            [reagent.core :as reagent]
            [re-frame.core :refer [dispatch subscribe]]
            [reactive-console.common :as common]
            [cljsjs.codemirror]
            [cljsjs.codemirror.addon.edit.matchbrackets]
            [cljsjs.codemirror.addon.runmode.runmode]
            [cljsjs.codemirror.addon.runmode.colorize]
            [cljsjs.codemirror.mode.clojure]))

(def default-cm-opts
  {:should-go-up
   (fn [source inst]
     (let [pos (.getCursor inst)]
       (= 0 (.-line pos))))

   :should-go-down
   (fn [source inst]
     (let [pos (.getCursor inst)
           last-line (.lastLine inst)]
       (= last-line (.-line pos))))})

(defn handlers [console-key]
  {:add-input    #(dispatch [:add-console-input console-key %1 %2])
   :add-result   #(dispatch [:add-console-result console-key %1 %2])
   :go-up        #(dispatch [:console-go-up console-key %])
   :go-down      #(dispatch [:console-go-down console-key %])
   :clear-items  #(dispatch [:clear-console-items console-key %])
   :set-text     #(dispatch [:console-set-text console-key %1])
   :add-log      #(dispatch [:add-console-log console-key %])})

(defn move-to-end
  [cm]
  (let [last-line (.lastLine cm)
        last-ch (count (.getLine cm last-line))]
    (.setCursor cm last-line last-ch)))

(defn modifying-prompt?
  [inst key]
  (let [pos (.getCursor inst)
        lno (.-line pos)
        cno (.-ch pos)
        compare-position-fn (if (= 8 key) <= <)]
    (and ((complement #{37 38 39 40}) key)
         (zero? lno)
         (compare-position-fn cno (common/beginning-of-source (.getValue inst))))))

(defn editor
  [console-key value-atom]
  (let [cm (subscribe [:get-console console-key])
        {:keys [add-input
                add-result
                should-go-up
                should-go-down
                go-up
                go-down
                set-text]} (merge (handlers console-key) default-cm-opts)
        eval-opts (subscribe [:get-console-eval-opts console-key])]

    (reagent/create-class
     {:component-did-mount
      (fn [this]
        (let [el (reagent/dom-node this)
              inst (js/CodeMirror.
                    el
                    (clj->js
                     {:lineNumbers false
                      :viewportMargin js/Infinity
                      :lineWrapping true
                      :matchBrackets true
                      :autofocus true
                      :extraKeys #js {"Shift-Enter" "newlineAndIndent"}
                      :value (str ((:get-prompt @eval-opts)) @value-atom)
                      :mode "clojure"}))]
          (dispatch [:add-console-instance console-key inst])
          (move-to-end inst)

          (.on inst "viewportChange"
               (fn []
                 (let [el (reagent/dom-node this)
                       cm-console (.-parentElement el)
                       box (.-parentElement cm-console)]
                   (common/scroll-to-el-bottom! box))))

          (.on inst "change"
               (fn []
                 (let [value (common/source-without-prompt (.getValue inst))]
                   (when-not (= value @value-atom)
                     (set-text value)))))

          (.on inst "keydown"
               (fn [inst evt]
                 (if (modifying-prompt? inst (.-keyCode evt))
                   (.preventDefault evt)
                   (case (.-keyCode evt)
                     ;; enter
                     13 (let [source (common/source-without-prompt (.getValue inst))]
                          (when ((:should-eval @eval-opts) source inst evt)
                            (.preventDefault evt)
                            ((:evaluate @eval-opts) #(dispatch [:on-eval-complete console-key %]) source)))
                     ;; up
                     38 (let [source (common/source-without-prompt (.getValue inst))]
                          (when (and (not (.-shiftKey evt))
                                     (should-go-up source inst))
                            (.preventDefault evt)
                            (go-up)))
                     ;; down
                     40 (let [source (common/source-without-prompt (.getValue inst))]
                          (when (and (not (.-shiftKey evt))
                                     (should-go-down source inst))
                            (.preventDefault evt)
                            (go-down)))
                     :none))))))

      :component-did-update
      (fn [this old-argv]
        (when-not (= @value-atom (common/source-without-prompt (.getValue @cm)))
          (.setValue @cm (str ((:get-prompt @eval-opts)) @value-atom))
          (move-to-end @cm)))

      :reagent-render
      (fn []
        @value-atom
        [:div])})))
