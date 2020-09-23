(ns rolling-rules.core
  (:require [uix.dom.alpha :as uix.dom]
            [uix.core.alpha :as uix]
            [xframe.core.alpha :as xf :refer [<sub]]
            [ajax.core :as ajax]
            ["react" :as react]
            ["framer-motion" :refer [motion AnimatePresence useAnimation]]
            
            [rolling-rules.fx]
            [rolling-rules.subs]
            [rolling-rules.util :as util]
            [rolling-rules.firebase :as firebase]
            [rolling-rules.firebase-auth-ui :as auth-ui]
            ))

;(set! *warn-on-infer* true)

(defn ! [& evt]
  (apply xf/dispatch evt))

(defn <- [k]
  (<sub [:get k]))

(defn rules []
  (uix/with-effect []
    (firebase/collection-on-snapshot ["rules"] #(xf/dispatch [:set [:rules] %])))
  (util/for-idx i [rule (<sub [:get :rules])]
                [:div.rule (pr-str rule)]))

(defn logged-in []
  [:<> 
   (util/for-idx i [dice (<sub [:get :dice])]
                     [:div.dice {:on-click #(xf/dispatch [:set])}
                      (:val dice)])
   
   [:button {:on-click #(xf/dispatch [:roll])}
    "roll"]
   [rules]
   [:button {:on-click #(xf/dispatch [:firebase/sign-out])}
    "log out"]])

(defn main []
  [:div#content 
   (if (<- :firebase/user)
     [logged-in]
     [auth-ui/login-create-account])])

(defn ^:dev/after-load start []
  (js/console.log "start")
  (uix.dom/render [main] (.getElementById js/document "app"))
  (firebase/init))

(defn init []
  (js/console.log "init")
  (xf/dispatch [:db/init])
  (start))