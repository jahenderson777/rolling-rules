(ns rolling-rules.fx
  (:require [xframe.core.alpha :as xf]
            [ajax.core :as ajax]
            ["firebase" :as firebase]))

(xf/reg-fx :http-get
           (fn [_ [_ {:keys [url on-ok on-failed]}]]
             (ajax/GET url {:handler #(xf/dispatch [on-ok %])
                            :error-handler (if on-failed
                                             #(xf/dispatch [on-failed %])
                                             println)
                            :keywords? true
                            :response-format :json})))

(xf/reg-fx :http-post
           (fn [_ [_ {:keys [url params]}]]
             (ajax/POST url {:handler println
                             :error-handler println
                             :params params
                             :format :raw})))

(xf/reg-event-fx
 :db/init
 (fn [_ _]
   {:db {:dice [{:faces 4 :val 4} 
                {:faces 4 :val 4}
                {:faces 4 :val 4}
                {:faces 4 :val 4}
                {:faces 6 :val 6}
                ]
         :window-size [js/window.innerWidth js/window.innerHeight]}}))

(xf/reg-event-db
 :set
 (fn [db [_ ks v]]
   (assoc-in db ks v)))

(xf/reg-event-db
 :roll
 (fn [{:keys [dice] :as db} [_]]
   (update db :dice
          (fn [dice]
            (for [d dice] 
              (assoc d :val (+ 1 (rand-int (:faces d)))))))))