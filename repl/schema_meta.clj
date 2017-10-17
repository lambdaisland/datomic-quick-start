(ns repl.schema-meta
  (:require [datomic.api :as d]))

(def db-uri "datomic:mem://quick-start-db")
;; (def db-uri "datomic:free://localhost:4334/quick-start-db")

(def schema [{:db/ident       :todo/title
              :db/doc         "The title of a todo item"
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one}

             {:db/ident       :todo/done?
              :db/doc         "Is the todo done?"
              :db/valueType   :db.type/boolean
              :db/cardinality :db.cardinality/one}])

(d/delete-database db-uri)
(d/create-database db-uri)

(def conn (d/connect db-uri))

(d/transact conn [{:db/ident       :user/name
                   :db/valueType   :db.type/string
                   :db/cardinality :db.cardinality/one
                   :db/unique      :db.unique/identity}])

(def tx-result
  (d/transact conn [{:user/name     "jillosaurus"}
                    {:user/name     "jonnyboy"}]))

(d/q '[:find ?e ?name
       :where [?e :user/name ?name]]
     (d/db conn))

(d/as-of (d/db conn) #inst "2017-10-12T10:16:31.876-00:00")

(d/q '[:find ?e ?name
       :where
       [?e :user/name ?name]
       [?e :user/admin? true]]
     (d/db conn))
;;=> #{[17592186045423 "plexus"]}


(d/q '[:find ?e ?a-ident ?v
       :where
       [?u :user/name   "plexus"]
       [?u :user/admin? true      ?t]
       [?e ?a           ?v        ?t]
       [?a :db/ident    ?a-ident]]
     (d/db conn))
;;=>
#{[17592186045423 :user/name "plexus"] [17592186045424 :user/name "hugabug"]
  [17592186045423 :user/admin? true]
  [17592186045424 :user/friends 17592186045423]
  [17592186045423 :user/friends 17592186045418]
  [13194139534318 :db/txInstant #inst "2017-10-12T11:12:38.655-00:00"]}

(d/q '[:find ?username ?friendname
       :where
       [?user   :user/friends ?friend]
       [?user   :user/name    ?username]
       [?friend :user/name    ?friendname]]
     (d/db conn))
;;=> #{["plexus" "jillosaurus"] ["hugabug" "plexus"]}

(d/q '[:find ?e
       :where
       [?e :user/name]]
     (d/db conn))
;;=> #{[17592186045418] [17592186045419] [17592186045423] [17592186045424]}
(class tx-result)
;;=> datomic.promise$settable_future$reify__5815

(keys @result)
;;=> (:db-before :db-after :tx-data :tempids)

(:tx-data @tx-result)
[#datom[13194139534316 50 #inst "2017-10-12T10:16:31.876-00:00" 13194139534316 true] #datom[17592186045421 63 "jillosaurus" 13194139534316 true] #datom[17592186045422 63 "jonnyboy" 13194139534316 true]]
;;=>
;;=>
[#datom[13194139534313 50 #inst "2017-10-12T09:46:18.239-00:00" 13194139534313 true]
 #datom[17592186045418 63 "Learn Datomic" 13194139534313 true]
 #datom[17592186045419 63 "Brew tea" 13194139534313 true]]

(d/ident (:db-after @tx-result) 50)            ;;=> :db/txInstant
(d/ident (:db-after @tx-result) 63)            ;;=> :user/name

(d/q '[:find ?e ?title ?t ?added
       :where [?e :todo/title ?title ?t ?added]]
     (d/db conn))

(d/transact conn [[:db/add (d/tempid :db.part/user) :todo/title "hello"]])

(d/entity (d/db conn) )

(d/touch
 (d/entity (d/db conn) 63))                    ;;=> #:db{:id 63, :ident :user/name, :valueType :db.type/string, :cardinality :db.cardinality/one}

(d/entid (d/db conn) :user/name)               ;;=> 63

(d/entity (d/db conn) :user/name)
;;=> #:db{:id 63}

(d/entity (d/db conn) 17592186045421)
;;=> #:db{:id 17592186045421}

(:db/valueType (d/entity (d/db conn) :user/name))
;;=> :db.type/string

(d/touch (d/entity (d/db conn) :user/name))
;;=> #:db{:id 63, :ident :user/name, :valueType :db.type/string, :cardinality :db.cardinality/one}

(d/touch (d/entity (d/db conn) 17592186045421))
;;=> {:db/id 17592186045421, :user/name "jillosaurus"}

(:basisT (d/db conn))

(d/q '[:find ?e ?name
       :where [?e :user/name ?name]]
     (d/db conn))
;;=> #{[17592186045418 "jillosaurus"] [17592186045419 "jonnyboy"]}

(d/q '[:find ?e ?name
       :in $ ?e
       :where [?e :user/name ?name]]
     (d/db conn)
     [:user/name "jillosaurus"])

(def promise (d/transact (d/connect db-uri) schema))
;;=> #datomic.promise/settable-future/reify--5815[{:status :ready, :val {:db-before datomic.db.Db@2fc9922a, :db-after datomic.db.Db@db346273, :tx-data [#datom[13194139534313 50 #inst "2017-10-04T12:09:29.919-00:00" 13194139534313 true] #datom[0 19 64 13194139534313 true] #datom[0 19 63 13194139534313 true]], :tempids {-9223301668109598142 63, -9223301668109598141 64}}} 0x167f835d]

(keys @promise)
;;=> (:db-before :db-after :tx-data :tempids)

(:tx-data @promise)
;;=>
[#datom[13194139534312 50 #inst "2017-10-04T12:11:13.126-00:00" 13194139534312 true]

 #datom[63 10 :todo/title 13194139534312 true]
 #datom[63 62 "The title of a todo item" 13194139534312 true]
 #datom[63 40 23 13194139534312 true]
 #datom[63 41 35 13194139534312 true]

 #datom[64 10 :todo/done? 13194139534312 true]
 #datom[64 62 "The title of a todo item" 13194139534312 true]
 #datom[64 40 24 13194139534312 true]
 #datom[64 41 35 13194139534312 true]

 #datom[0 13 64 13194139534312 true]
 #datom[0 13 63 13194139534312 true]]

(d/pull (d/db (d/connect db-uri)) '[*] 13194139534312)
;;=> #:db{:id 13194139534312, :txInstant #inst "2017-10-04T12:11:13.126-00:00"}

(d/pull (d/db (d/connect db-uri)) '[*] 63)
;;=> #:db{:id 63, :ident :todo/title, :valueType #:db{:id 23}, :cardinality #:db{:id 35}, :doc "The title of a todo item"}

(d/pull (d/db (d/connect db-uri)) '[*] 64)
;;=> #:db{:id 64, :ident :todo/done?, :valueType #:db{:id 24}, :cardinality #:db{:id 35}, :doc "The title of a todo item"}

(d/pull (d/db (d/connect db-uri)) '[*] 13)
;;=> #:db{:id 13, :ident :db.install/attribute, :valueType #:db{:id 20}, :cardinality #:db{:id 36}, :doc "System attribute with type :db.type/ref. Asserting this attribute on :db.part/db with value v will install v as an attribute."}

(d/pull (d/db (d/connect db-uri)) '[*] 40)
;;=> #:db{:id 40, :ident :db/valueType, :valueType #:db{:id 20}, :cardinality #:db{:id 35}, :doc "Property of an attribute that specifies the attribute's value type. Built-in value types include, :db.type/keyword, :db.type/string, :db.type/ref, :db.type/instant, :db.type/long, :db.type/bigdec, :db.type/boolean, :db.type/float, :db.type/uuid, :db.type/double, :db.type/bigint,  :db.type/uri."}

(d/pull (d/db (d/connect db-uri)) '[*] :db/ident)

(:tx-data @(d/transact (d/connect db-uri) [#:todo{:title "remember the milk" :done? false}]))
;;=>
;;=>
[#datom[13194139534313 50 #inst "2017-10-04T12:16:45.706-00:00" 13194139534313 true]
 #datom[17592186045418 63 "remember the milk" 13194139534313 true]
 #datom[17592186045418 64 false 13194139534313 true]]


(class (first (:tx-data @(d/transact (d/connect db-uri) [#:todo{:title "remember the milk" :done? false}]))))
;;=> datomic.db.Datum

(datomic.db.Datum. 17592186045418 64 false 13194139534313)

(clojure.repl/dir)

(require '[clojure.reflect :as r])

(map :name (:members (r/reflect (datomic.db.Datum. 17592186045418 64 false 13194139534313))))
;;=> (equals const__8 getDoubleV const__29 tx valAt getBasis hashCode a nth nth const__16 getE const__28 v getV getT getBooleanV a eidx const__23 const__27 getA getP e valAt const__5 added const__25 getLongV const__12 tOp v getTx count e getIntV get datomic.db.Datum const__14 const__26 isAssertion getFloatV)

(defn datom-seq [datom]
  (list (.getE datom) (.getA datom) (.getV datom) (.getTx datom)))



(defn ident [eid]
  (when (int? eid)
    (d/q '[:find ?i .
           :in $ ?e
           :where [?e :db/ident ?i]]
         (d/db (d/connect db-uri)) eid)))

(defn with-idents [txdata]
  (mapv (fn [datom] (mapv #(or (ident %) %) (datom-seq datom))) txdata))

(with-idents (:tx-data @(d/transact (d/connect db-uri) [#:todo{:title "remember the milk" :done? false}])))
;;=>
[[13194139534321 :db/txInstant #inst "2017-10-04T12:28:18.058-00:00" 13194139534321]
 [17592186045426 :todo/title "remember the milk" 13194139534321]
 [17592186045426 :todo/done? false 13194139534321]]

(with-idents (:tx-data @promise))
;;=>
[[13194139534312 :db/txInstant #inst "2017-10-04T12:28:35.513-00:00" 13194139534312]
 [:todo/title :db/ident :todo/title 13194139534312]
 [:todo/title :db/doc "The title of a todo item" 13194139534312]
 [:todo/title :db/valueType :db.type/string 13194139534312]
 [:todo/title :db/cardinality :db.cardinality/one 13194139534312]
 [:todo/done? :db/ident :todo/done? 13194139534312]
 [:todo/done? :db/doc "The title of a todo item" 13194139534312]
 [:todo/done? :db/valueType :db.type/boolean 13194139534312]
 [:todo/done? :db/cardinality :db.cardinality/one 13194139534312]
 [:db.part/db :db.install/attribute :todo/done? 13194139534312]
 [:db.part/db :db.install/attribute :todo/title 13194139534312]]

true


;;=> datomic.db.Db@1e985da6

(:db-after @tx-result)
;;=> datomic.db.Db@ad344ee4

(d/q '[:find ?e :where [?e :user/name "jillosaurus"]] (:db-before @tx-result))
;;=> #{}

(d/q '[:find ?e :where [?e :user/name "jillosaurus"]] (:db-after @tx-result))
;;=> #{[17592186045418]}
