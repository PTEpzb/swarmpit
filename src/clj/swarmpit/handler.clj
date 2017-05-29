(ns swarmpit.handler
  (:require [bidi.ring :refer [make-handler]]
            [clojure.walk :as walk]
            [swarmpit.api :as api]
            [swarmpit.token :as token]))

(defn json-error
  [status response]
  {:status  status
   :headers {"Content-Type" "application/json"}
   :body    {:error response}})

(defn json-ok
  ([] {:status 200})
  ([status response] {:status status
                      :body   response}))

;;; Login handler

(defn login
  [{:keys [headers]}]
  (let [token (get headers "authorization")]
    (if (nil? token)
      (json-error 400 "Missing token")
      (let [user (->> (token/decode-basic token)
                      (api/user-by-credentials))]
        (if (nil? user)
          (json-error 401 "Invalid credentials")
          (json-ok 200 {:token (token/generate-jwt user)}))))))

;;; User handler

(defn users
  [_]
  (->> (api/users)
       (json-ok 200)))

;;; Registry handler

(defn registries
  [_]
  (->> (api/registries)
       (json-ok 200)))

(defn registry-create
  [{:keys [params]}]
  (let [payload (walk/keywordize-keys params)]
    (api/repository payload)
    (->> (api/create-registry payload)
         (json-ok 201))))

;;; Service handler

(defn services
  [_]
  (->> (api/services)
       (json-ok 200)))

(defn service
  [{:keys [route-params]}]
  (->> (api/service (:id route-params))
       (json-ok 200)))

(defn service-create
  [{:keys [params]}]
  (let [payload (walk/keywordize-keys params)]
    (->> (api/create-service payload)
         (json-ok 201))))

(defn service-update
  [{:keys [route-params params]}]
  (let [payload (walk/keywordize-keys params)]
    (api/update-service (:id route-params) payload)
    (json-ok)))

(defn service-delete
  [{:keys [route-params]}]
  (api/delete-service (:id route-params))
  (json-ok))

;;; Network handler

(defn networks
  [_]
  (->> (api/networks)
       (json-ok 200)))

(defn network
  [{:keys [route-params]}]
  (->> (api/network (:id route-params))
       (json-ok 200)))

(defn network-create
  [{:keys [params]}]
  (let [payload (walk/keywordize-keys params)]
    (->> (api/create-network payload)
         (json-ok 201))))

(defn network-delete
  [{:keys [route-params]}]
  (api/delete-network (:id route-params))
  (json-ok))

;;; Node handler

(defn nodes
  [_]
  (->> (api/nodes)
       (json-ok 200)))

(defn node
  [{:keys [route-params]}]
  (->> (api/node (:id route-params))
       (json-ok 200)))

;;; Task handler

(defn tasks
  [_]
  (->> (api/tasks)
       (json-ok 200)))

(defn task
  [{:keys [route-params]}]
  (->> (api/task (:id route-params))
       (json-ok 200)))

;;; Repository handler

(defn repositories
  [_]
  (->> (api/repositories)
       (json-ok 200)))

;;; Handler

(def handler
  (make-handler ["/" {"login"      {:post login}
                      "users"      {:get users}
                      "registries" {:get  registries
                                    :post registry-create}
                      "services"   {:get  services
                                    :post service-create}
                      "services/"  {:get    {[:id] service}
                                    :delete {[:id] service-delete}
                                    :post   {[:id] service-update}}
                      "networks"   {:get  networks
                                    :post network-create}
                      "networks/"  {:get    {[:id] network}
                                    :delete {[:id] network-delete}}
                      "nodes"      {:get nodes}
                      "nodes/"     {:get {[:id] node}}
                      "tasks"      {:get tasks}
                      "tasks/"     {:get {[:id] task}}
                      "repository" {:get repositories}}]))