(ns rolling-rules.util
  (:require-macros [rolling-rules.util]))

(defn hsl
  ([hue sat lightness]
   (hsl hue sat lightness 1))
  ([hue sat lightness alpha]
   (str "hsla(" hue ", " sat "%, " lightness "%, " alpha ")")))

(defn gradient [direction & stops]
  (str "linear-gradient(" direction "deg,"
       (apply str (interpose ", "
                             (for [[h s l a pct-pos] stops]
                               (str (hsl h s l a) " " pct-pos "% "))))
       ")"))