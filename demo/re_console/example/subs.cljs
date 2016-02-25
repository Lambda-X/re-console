(ns re-console.example.subs
  (:require
   [re-frame.core :refer [register-sub]]
   [clairvoyant.core :refer-macros [trace-forms]]
   [re-frame-tracer.core :refer [tracer]])
  (:require-macros
   [reagent.ratom :refer [reaction]]))

;; (trace-forms {:tracer (tracer :color "brown")}

(register-sub
 :get-console-verbose
 (fn [db [_ console-key]]
   (reaction (get-in @db [:consoles (name console-key) :verbose]))))

;; )
