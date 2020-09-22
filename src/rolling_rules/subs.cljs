(ns rolling-rules.subs
  (:require [xframe.core.alpha :as xf]))

(xf/reg-sub
 :get
 (fn [& ks]
   (get-in (xf/<- [::xf/db]) ks)))