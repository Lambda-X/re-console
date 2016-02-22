(ns re-console.example.handlers
  (:require [re-frame.core :refer [register-handler dispatch]]
            [clairvoyant.core :refer-macros [trace-forms]]
            [re-frame-tracer.core :refer [tracer]]))

;; (trace-forms {:tracer (tracer :color "green")}

(register-handler
 :init-verbose
 (fn init-verbose [db [_  console-key]]
   (assoc-in db [:consoles (name console-key) :verbose] false)))


(register-handler
 :toggle-verbose
 (fn toggle-verbose [db [_  console-key]]
   (update-in db [:consoles (name console-key) :verbose] not)))

;; )
