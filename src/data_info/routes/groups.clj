(ns data-info.routes.groups
  (:use [clojure-commons.error-codes]
        [common-swagger-api.schema]
        [otel.middleware :only [otel-middleware]]
        [data-info.routes.schemas.users])
  (:require [data-info.services.groups :as groups]
            [common-swagger-api.schema.data :as data-schema]
            [schema.core :as s]
            [ring.util.http-response :refer [ok]]))

(s/defschema Group
  {:name NonBlankString
   :members [NonBlankString]})

(s/defschema GroupMembers
  (dissoc Group :name))

(s/defschema GroupErrorResponses
  (merge ErrorResponseUnchecked
         {(s/optional-key :user) NonBlankString
          (s/optional-key :users) [NonBlankString]
          (s/optional-key :group) NonBlankString}))

(s/defschema CreateErrorCodes
  (apply s/enum (conj data-schema/CommonErrorCodeResponses ERR_NOT_A_USER ERR_EXISTS)))

(s/defschema GetUpdateErrorCodes
  (apply s/enum (conj data-schema/CommonErrorCodeResponses ERR_NOT_A_USER ERR_DOES_NOT_EXIST)))

(s/defschema DeleteErrorCodes
  (apply s/enum (conj data-schema/CommonErrorCodeResponses ERR_NOT_A_USER)))

(s/defschema CreateErrorResponses
  (assoc GroupErrorResponses
         :error_code CreateErrorCodes))

(s/defschema GetUpdateErrorResponses
  (assoc GroupErrorResponses
         :error_code GetUpdateErrorCodes))

(s/defschema DeleteErrorResponses
  (assoc GroupErrorResponses
         :error_code DeleteErrorCodes))

(s/defschema ListParams
  (assoc StandardUserQueryParams
         :prefix String))

(defroutes groups-routes
  (context "/groups" []
    :tags ["groups"]

    (GET "/" []
      :middleware  [otel-middleware]
      :query       [params ListParams]
      :summary     "List groups"
      :description "List groups (as qualified names) given a prefix to match"
      (ok (groups/list-groups params)))

    (POST "/" []
      :middleware  [otel-middleware]
      :query       [params StandardUserQueryParams]
      :body        [body Group]
      :responses   (merge CommonResponses
                          {500 {:schema CreateErrorResponses
                                :description data-schema/CommonErrorCodeDocs}}
                          {403 {:schema ErrorResponseForbidden
                                :description "No access to group administration"}}
                          {200 {:schema Group
                                :description "Successful response"}})
      :summary     "Create group"
      :description "Create an IRODS group given a name and a list of members"
      (ok (groups/create-group params body)))

    (context ["/:group-name"] []
      :path-params [group-name :- (describe NonBlankString "The name of an iRODS group, with or without zone qualification")]

      (GET "/" []
        :middleware  [otel-middleware]
        :query       [params StandardUserQueryParams]
        :responses   (merge CommonResponses
                            {500 {:schema GetUpdateErrorResponses
                                  :description data-schema/CommonErrorCodeDocs}}
                            {200 {:schema Group
                                  :description "Successful response"}})
        :summary     "List group members"
        :description "List an IRODS group & members given a name"
        (ok (groups/get-group params group-name)))

      (PUT "/" []
        :middleware  [otel-middleware]
        :query       [params StandardUserQueryParams]
        :body        [body GroupMembers]
        :responses   (merge CommonResponses
                            {500 {:schema GetUpdateErrorResponses
                                  :description data-schema/CommonErrorCodeDocs}}
                            {403 {:schema ErrorResponseForbidden
                                  :description "No access to group administration"}}
                            {200 {:schema Group
                                  :description "Successful response"}})
        :summary     "Update group members"
        :description "Update an IRODS group's members"
        (ok (groups/update-group-members params body group-name)))

      (DELETE "/" []
        :middleware  [otel-middleware]
        :query       [params StandardUserQueryParams]
        :responses   (merge CommonResponses
                            {500 {:schema DeleteErrorResponses
                                  :description data-schema/CommonErrorCodeDocs}}
                            {403 {:schema ErrorResponseForbidden
                                  :description "No access to group administration"}}
                            {200 {:description "Successful response"}})
        :summary     "Delete group"
        :description "Delete an IRODS group's members"
        (ok (groups/delete-group params group-name))))))
