(ns rolling-rules.firebase
  (:require [clojure.string :as str]
            [xframe.core.alpha :as xf]
            ["firebase" :as firebase]
            ["firebase/auth" :as firebase-auth]))

(defonce firebase-app-info
  {:apiKey "AIzaSyDrq5xXw3h9hNJfFmH2x1cprsQ165PYe28"
   :authDomain "rolling-rules.firebaseapp.com"
   :databaseURL "https://rolling-rules.firebaseio.com"
   :projectId "rolling-rules"
   :storageBucket "rolling-rules.appspot.com"
   :messagingSenderId "725862460631"
   :appId "1:725862460631:web:35f5e63a6a79565c8e5492"})

(defn- user [firebase-user]
  (when firebase-user
    {:uid           (.-uid firebase-user)
     :provider-data (.-providerData firebase-user)
     :display-name  (.-displayName firebase-user)
     :photo-url     (.-photoURL firebase-user)
     :email         (or (.-email firebase-user)
                        (let [provider-data (.-providerData firebase-user)]
                          (when-not (empty? provider-data)
                            (-> provider-data first .-email))))}))

(defn- default-error-handler [error]
  (println "Error: " error)) 

(defn- set-user [firebase-user]
  (println "called set-user")
  (xf/dispatch [:set [:firebase/user] (user firebase-user)]))

(defn init []
  (defonce _init
    (do
      (firebase/initializeApp (clj->js firebase-app-info))
      (.. firebase auth (onAuthStateChanged set-user default-error-handler))
      (let [RecaptchaVerifier (.. firebase -auth -RecaptchaVerifier)
            recaptcha (RecaptchaVerifier.
                       "Phone"
                       (clj->js {:size "invisible"
                                 :callback #(xf/dispatch [:set [:firebase/captcha-msg] "Welcome Human"])}))]
        (xf/dispatch [:set [:firebase/recaptcha-verifier] recaptcha])
        (println "here")))))

(defn email-create-user [_ [_ {:keys [email password]}]]
  (.. firebase auth (createUserWithEmailAndPassword email password)
      (then (fn [user-credential]
              (set-user (.-user user-credential))))
      (catch default-error-handler)))

(xf/reg-fx :firebase/email-create-user email-create-user)
(xf/reg-event-fx
 :firebase/email-create-user
 (fn [db _]
   (let [{:firebase/keys [temp-email temp-password temp-password-confirm]} db]
     {:firebase/email-create-user {:email temp-email :password temp-password}
      :db (dissoc db :firebase/temp-email :firebase/temp-password :firebase/temp-password-confirm :firebase/temp-name)})))

(defn email-sign-in [_ [_ {:keys [email password]}]]
  (.. firebase auth (signInWithEmailAndPassword email password)
      (then (fn [user-credential]
              (set-user (.-user user-credential))))
      (catch default-error-handler)))

(xf/reg-fx :firebase/email-sign-in email-sign-in)
(xf/reg-event-fx
 :firebase/email-sign-in
 (fn [db _]
   (let [{:firebase/keys [temp-email temp-password]} db]
     {:firebase/email-sign-in {:email temp-email :password temp-password}
      :db (dissoc db :firebase/temp-email :firebase/temp-password :firebase/temp-password-confirm :firebase/temp-name)})))

(defn sign-out [_ _]
  (.. firebase auth (signOut)
      (catch default-error-handler)))

(xf/reg-fx :firebase/sign-out sign-out)
(xf/reg-event-fx :firebase/sign-out (fn [_ _] {:firebase/sign-out true}))

(defn phone-sign-in [{:firebase/keys [temp-phone recaptcha-verifier] :as db} _]
  (let [phone-number (str/replace-first temp-phone #"^[0]+" "+44")]
    (if recaptcha-verifier
      (.. firebase auth
          (signInWithPhoneNumber phone-number recaptcha-verifier)
          (then (fn [confirmation]
                  (xf/dispatch [:set [:firebase/sms-sent] true])
                  (xf/dispatch [:set [:firebase/recaptcha-confirmation-result] confirmation])))
          (catch default-error-handler))
      (.warn js/console "Initialise reCaptcha first"))))

(xf/reg-fx :firebase/phone-sign-in phone-sign-in)
(xf/reg-event-fx :firebase/phone-sign-in (fn [_ _] {:firebase/phone-sign-in true}))

(defn phone-confirm-code [{:firebase/keys [recaptcha-confirmation-result temp-sms-code] :as db} _]
  (if [recaptcha-confirmation-result]
    (.. recaptcha-confirmation-result
        (confirm temp-sms-code)
        (then set-user)
        (catch default-error-handler))
    (.warn js/console "reCaptcha confirmation missing")))

(xf/reg-fx :firebase/phone-confirm-code phone-confirm-code)
(xf/reg-event-fx :firebase/phone-confirm-code (fn [_ _] {:firebase/phone-confirm-code true}))

(defn send-password-reset-email [_ [_ {:keys [email on-complete]}]]
  (.. firebase auth
      (sendPasswordResetEmail email)
      (then on-complete)
      (catch default-error-handler)))

(xf/reg-fx :firebase/send-password-reset-email send-password-reset-email)
(xf/reg-event-fx
 :firebase/send-password-reset-email
 (fn [db _]
   (let [{:firebase/keys [temp-email]} db]
     {:firebase/send-password-reset-email {:email temp-email
                                           :on-complete #(js/alert "Password reset email sent")}})))