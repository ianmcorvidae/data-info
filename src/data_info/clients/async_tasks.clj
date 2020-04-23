(ns data-info.clients.async-tasks
  (:use [slingshot.slingshot :only [try+]])
  (:require [data-info.util.config :as config]
            [data-info.util.irods :as irods]
            [clojure.tools.logging :as log]
            [async-tasks-client.core :as async-tasks-client]))


(defn get-by-id
  [id]
  (async-tasks-client/get-by-id (config/async-tasks-client) id))

(defn delete-by-id
  [id]
  (async-tasks-client/delete-by-id (config/async-tasks-client) id))

(defn create-task
  [task]
  (async-tasks-client/create-task (config/async-tasks-client) task))

(defn add-status
  [id status]
  (async-tasks-client/add-status (config/async-tasks-client) id status))

(defn add-completed-status
  [id status]
  (async-tasks-client/add-completed-status (config/async-tasks-client) id status))

(defn add-behavior
  [id behavior]
  (async-tasks-client/add-behavior (config/async-tasks-client) id behavior))

(defn get-by-filter
  [filters]
  (async-tasks-client/get-by-filter (config/async-tasks-client) filters))

(defn paths-async-thread
  [async-task-id jargon-fn]
  (let [{:keys [username] :as async-task} (get-by-id async-task-id)
        update-fn (fn [path action]
                    (log/info "Updating async task:" async-task-id ":" path action)
                    (add-status async-task-id {:status "running" :detail (format "%s: %s" path (name action))}))]
    (try+
      (add-status async-task-id {:status "started"})
      (irods/with-jargon-exceptions :client-user username [cm]
        (jargon-fn cm async-task update-fn))
      (add-completed-status async-task-id {:status "completed"})
      (catch Object _
        (log/error (:throwable &throw-context) "failed processing async task" async-task-id)
        (add-completed-status async-task-id {:status "failed"})))))
