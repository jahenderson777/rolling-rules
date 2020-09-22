(ns rolling-rules.firebase-auth-ui
  (:require [uix.dom.alpha :as uix.dom]
            [uix.core.alpha :as uix]
            [xframe.core.alpha :as xf :refer [<sub]]
            [ajax.core :as ajax]
            ["react" :as react]
            ["framer-motion" :refer [motion AnimatePresence useAnimation]]

            [rolling-rules.fx]
            [rolling-rules.subs]
            [rolling-rules.util :as util]))

(defn ! [& evt]
  (xf/dispatch evt))

(defn <- [k]
  (<sub [:get k]))

(defn button [props text]
  [:button (merge ;{:style (s :pa3 :mb3 :white)}
                  props)
   text])

(defn login-input [{:keys [id db-key type size on-return]}]
  (let [val (<- db-key)
        ref (atom nil)]
    [:div {:style (merge ;(s :pa2)
                         {:display "block"})}
     [:input {;:style (s :f3 :pa2)
              :type type
              :id id
              :name id
              :size (or size 22)
              :value (or val "")
              :placeholder id
              :ref #(reset! ref %)
              :on-key-press (fn [e]
                              (when (= 13 (.-charCode e))
                                (.blur @ref)
                                (when on-return
                                  (on-return))))
              :on-change #(! :set [db-key] (-> % .-target .-value))}]]))

(defn login-create-account []
  [:<>
    (if (<- :firebase/sms-sent)
      [:div
       [:div "Please enter the code we just sent you..."]
       [login-input {:id "SMS Code" :db-key :firebase/temp-sms-code :type "phone"
                     :on-return #(! :firebase/phone-confirm-code)}]
       [button {:on-click #(! :firebase/phone-confirm-code)}
        "Confirm code"]
       [:div ;{:style (s :pa3)}
        [:a {:on-click #(do (! :set [:firebase/temp-sms-code] nil)
                            (! :set [:firebase/sms-sent] nil))}
         "abort"]]]
      [:div
       [:p ;{:style (s :mb3)} 
        "Login with your mobile phone..."]
       [login-input {:id "Phone" :db-key :firebase/temp-phone :type "phone"
                     :on-return #(! :firebase/phone-sign-in)}]
       [button {:id "phone-sign-in-button"
                :on-click #(! :firebase/phone-sign-in)}
        "SMS me a login code"]])
    [:hr]
    (if (<- :firebase/show-login-account)
      [:div
       [:p ;{:style (s :ma4)} 
        "Login with an account you have already created..."]
       [login-input {:id "Email" :db-key :firebase/temp-email :type "email"}]
       [login-input {:id "Password" :db-key :firebase/temp-password :type "password"}]
       [:div ;{:style (s :ma3)}
        [button {:on-click #(! :firebase/email-sign-in)}
         "Login"]
        [:a {:style {:margin-left 3 :padding 10 :font-size 14 :text-decoration "underline" :cursor "pointer"} 
             :on-click #(! :firebase/send-password-reset-email)} "forgot password?"]]
       [:div ;{:style (s :mt3)}
        [:a {:style {:text-decoration "underline" :cursor "pointer"} :on-click #(! :set [:firebase/show-login-account] false)}
         "Create an account with your email address"]]]
      [:div
       [:p ;{:style (s :ma4)} 
        "Or create an account with your email address..."]
      ;[login-input {:id "Name" :db-key :firebase/temp-name :type "text"}]
       [login-input {:id "Email" :db-key :firebase/temp-email :type "email"}]
       [login-input {:id "Password" :db-key :firebase/temp-password :type "password"}]
       [login-input {:id "Confirm Password" :db-key :firebase/temp-password-confirm :type "password"}]
       [:div ;{:style (s :pt3)}
        [button {:on-click #(! :firebase/email-create-user)}
         "Create account"]]
       [:div ;{:style (s :mt3)}
        [:a {:style {:text-decoration "underline" :cursor "pointer"} :on-click #(! :set [:firebase/show-login-account] true)} 
         "Login with an existing account"]]])])
