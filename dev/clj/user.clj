(ns user
  (:require [reloaded.repl :refer [system init start stop go reset reset-all set-init!]]))

(set-init! #(do
              (require 'hakukohderyhmapalvelu.system)
              ((resolve 'hakukohderyhmapalvelu.system/hakukohderyhmapalvelu-system))))
