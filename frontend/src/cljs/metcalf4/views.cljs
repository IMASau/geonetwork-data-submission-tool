(ns metcalf4.views
  (:require [clojure.string :as string]))


; For pure views only, no re-frame subs/handlers

(defn user-display
  [user]
  (if (and (string/blank? (:lastName user)) (string/blank? (:firstName user)))
    (if (string/blank? (:email user))
      (:username user)
      (:email user))
    (str (:firstName user) " " (:lastName user))))

