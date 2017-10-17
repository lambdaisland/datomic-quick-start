(ns datomic-quick-start.core
  (:require [datomic.api :as d]))

(def db-uri "datomic:mem://quick-start-db")    ;;=> #'datomic-quick-start.core/db-uri

(d/delete-database db-uri)                     ;;=> true
(d/create-database db-uri)                     ;;=> true

(def conn (d/connect db-uri))                  ;;=> #'datomic-quick-start.core/conn

conn                                           ;;=> #object[datomic.peer.LocalConnection 0x5b090621 "datomic.peer.LocalConnection@5b090621"]

[31874  :user/name      "jillosaurus"]
[31874  :user/email     "jill@insect.club"]
[31874  :user/location  "Bug, Bamberg"]

{:db/id          31874
 :user/name      "jillosaurus"
 :user/email     "jill@insect.club"
 :user/location  "Bug, Bamberg"}

(d/transact conn [{;; :db/id       chosen by datomic
                   :db/ident       :user/name
                   :db/doc         "The unique username of a user."
                   :db/valueType   :db.type/string
                   :db/cardinality :db.cardinality/one
                   :db/unique      :db.unique/identity}])
(def tx-result
  (d/transact conn [{:user/name    "jillosaurus"}
                    {:user/name    "jonnyboy"}]))

(class tx-result)                              ;;=> datomic.promise$settable_future$reify__5815

(keys @tx-result)                              ;;=> (:db-before :db-after :tx-data :tempids)

(:db-before @tx-result)                        ;;=> datomic.db.Db@402a0ab6

(:db-after @tx-result)                         ;;=> datomic.db.Db@84a2fb05

(d/q '[:find ?e :where [?e :user/name "jillosaurus"]] (:db-after @tx-result))
;;=> #{[17592186045419]}

(d/q '[:find ?e :where [?e :user/name "jillosaurus"]] (:db-before @tx-result))
;;=> #{}

(d/db conn)                                    ;;=> datomic.db.Db@84a2fb05

(d/q '[:find ?e :where [?e :user/name "jillosaurus"]] (d/db conn))
;;=> #{[17592186045419]}












(def tx-result
  (d/transact conn [{:user/name    "jillosaurus"}
                    {:user/name    "jonnyboy"}]))

(keys @tx-result)                              ;;=> (:db-before :db-after :tx-data :tempids)

(:db-before @tx-result)                        ;;=> datomic.db.Db@84a2fb05

(:db-after @tx-result)                         ;;=> datomic.db.Db@84df0d0f

(:tx-data @tx-result)
;;=>
[#datom[13194139534316 50 #inst "2017-10-12T10:16:31" 13194139534316 true]
 #datom[17592186045421 63 "jillosaurus"               13194139534316 true]
 #datom[17592186045422 63 "jonnyboy"                  13194139534316 true]]
;;            🡑        🡑               🡑                    🡑         🡑
;;          entity   attribute        value             transaction  added?

(d/ident (d/db conn) 50)                       ;;=> :db/txInstant
(d/ident (d/db conn) 63)                       ;;=> "user/name"

(d/entity (d/db conn) :user/name)              ;;=> #:db{:id 63}
(d/entity (d/db conn) 63)                      ;;=> #:db{:id 63}

(:db/valueType (d/entity (d/db conn) :user/name))
;;=> :db.type/string

(d/touch (d/entity (d/db conn) :user/name))
;;=> #:db{:id 63, :ident :user/name, :valueType :db.type/string, :cardinality :db.cardinality/one, :unique :db.unique/identity, :doc "The unique username of a user."}

(d/touch (d/entity (d/db conn) 17592186045421))
;;=> #:db{:id 17592186045421, :user/name "jillosaurus"}




(d/transact conn [{:db/ident       :user/friends
                   :db/valueType   :db.type/ref
                   :db/cardinality :db.cardinality/many}

                  {:db/ident       :user/admin?
                   :db/valueType   :db.type/boolean
                   :db/cardinality :db.cardinality/one}])


(d/transact conn [{:db/id "user1"
                   :user/name "hugabug"
                   :user/friends #{"user2"}}

                  {:db/id "user2"
                   :user/name "plexus"
                   :user/admin? true
                   :user/friends #{[:user/name "jillosaurus"]}}])
(d/q '[:find ?e ?name
       :where [?e :user/name ?name]]
     (d/db conn))
;;=>
#{[17592186045420 "jonnyboy"]
  [17592186045423 "hugabug"]
  [17592186045424 "plexus"]
  [17592186045419 "jillosaurus"]}

(d/q '[:find ?name
       :where
       [?e :user/name ?name]
       [?e :user/admin? true]]
     ;; 🡑        🡑      🡑           🡑         🡑
     ;; entity   attr   value   transaction  added?
     (d/db conn))
;;=> #{["plexus"]}

(d/q '[:find ?e ?a-ident ?v
       :where
       [?u :user/name "plexus"]
       [?u :user/admin? true        ?t]
       [?e ?a           ?v          ?t]
       [?a :db/ident    ?a-ident]]
     ;; 🡑        🡑      🡑           🡑         🡑
     ;; entity   attr   value   transaction  added?
     (d/db conn))
;;=>
#{[17592186045423 :user/name "hugabug"]
  [17592186045423 :user/friends 17592186045424]
  [17592186045424 :user/name "plexus"]
  [17592186045424 :user/admin? true]
  [17592186045424 :user/friends 17592186045419]
  [13194139534318 :db/txInstant #inst "2017-10-17T10:45:49.954-00:00"]}

(d/q '[:find ?e
       :where [?e :user/name]]
     (d/db conn))
;;=> #{[17592186045419] [17592186045420] [17592186045423] [17592186045424]}

(d/q '[:find ?username ?friendname
       :where
       [?user    :user/friends  ?friend]
       [?user    :user/name     ?username]
       [?friend  :user/name     ?friendname]]
     (d/db conn))
;;=> #{["plexus" "jillosaurus"] ["hugabug" "plexus"]}

(defn friends-of [db username]
  (d/q '[:find ?friendname
         :in $ ?username
         :where
         [?user    :user/friends  ?friend]
         [?user    :user/name     ?username]
         [?friend  :user/name     ?friendname]]
       db
       username))

(friends-of (d/db conn) "plexus")
;;=> #{["jillosaurus"]}
