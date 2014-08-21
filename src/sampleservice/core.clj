(ns sampleservice.core
  "A sample API/webapp for experimenting with API regularity and visitation."
  (:require [com.stuartsierra.component :as component]
            [ring.adapter.jetty :as jetty]
            [compojure.core :refer [routes ANY DELETE GET POST PUT]]
            [compojure.handler :as hdr]
            [cheshire.core :as jsn])
  (:gen-class))

;; =============================================================================
;; Helper functions
;; =============================================================================

(defn- int-> [x] (Integer/parseInt x))


;; =============================================================================
;; Store
;; =============================================================================

(defn connect [host port]
  (println ";; Connecting to store")
  (reify java.io.Closeable
    (close [_] (println ";; Closing store connection"))))

(defrecord Store [host port connection]
  component/Lifecycle

  (start [component]
    (println ";; Starting Store")
    (let [conn (connect host port)]
      (assoc component :connection conn)))

  (stop [component]
    (println ";; Stopping Store")
    (.close connection)
    component))

(defn store [host port] (map->Store {:host host :port port}))

(def thingstore (atom {(int 1) "/things/thingy" (int 2) "/things/thingy2"}))

(defn store-things [store] (vals @thingstore))

(defn store-thing [store id] (@thingstore id))

(defn store-create-thing [store description]
  (let [k (int (inc (apply max (keys @thingstore))))]
    (swap! thingstore assoc k description)))

(defn store-edit-thing [store id description]
  (swap! thingstore assoc id description))

(defn store-delete-thing [store id] (swap! thingstore dissoc id))


;; =============================================================================
;; Service (API)
;; =============================================================================

(defrecord Service [options store]
  component/Lifecycle

  (start [this] (println ";; Starting Service") (assoc this :store store))

  (stop [this] (println ";; Stopping Service") this))

(defn service [cfg-options] (map->Service {:options cfg-options}))

(defn token [{:keys [store]}] {"access_token" "mytoken" "token_type" "bearer"})

(defn things [{:keys [store]}] (store-things store))
(defn things-* [{:keys [store]} id] (store-thing store id))
(defn thing-create [{:keys [store]} description]
  (store-create-thing store description))
(defn thing-edit [{:keys [store]} id description]
  (store-edit-thing store id description))
(defn thing-delete [{:keys [store]} id] (store-delete-thing store id))


;; =============================================================================
;; Server
;; =============================================================================

(defn default-rsp [body]
  (let [rsp {:headers {"Content-Type" "application/json"}}
        s (if body 200 404)
        rs (assoc rsp :status s)]
    (if body (assoc rs :body (jsn/generate-string body)) rs)))

(defn token-rsp [service] (default-rsp (token service)))

(defn things-rsp [service] (default-rsp (things service)))
(defn things-*-rsp [service {:keys [params]}]
  (default-rsp (things-* service (int-> (:id params)))))
(defn thing-create-rsp [service req]
  (let [desc (jsn/parse-string (slurp (:body req)))]
    (default-rsp (thing-create service (desc "description")))))
(defn thing-edit-rsp [service {:keys [params] :as req}]
  (let [desc (jsn/parse-string (slurp (:body req)))]
    (default-rsp (thing-edit service (int-> (:id params)) (desc "description")))))
(defn thing-delete-rsp [service {:keys [params]}]
  (default-rsp (thing-delete service (int-> (:id params)))))

(defn myroutes [service]
  (routes
    (GET    "/sampleservice/token"      []  (token-rsp service))
    (GET    "/sampleservice/things"     []  (things-rsp service))
    (GET    "/sampleservice/things/:id" req (things-*-rsp service req))
    (POST   "/sampleservice/things"     req (thing-create-rsp service req))
    (PUT    "/sampleservice/things/:id" req (thing-edit-rsp service req))
    (DELETE "/sampleservice/things/:id" req (thing-delete-rsp service req))))

(defrecord Server [port]
  component/Lifecycle
  (start [this]
    (println ";; Starting Server")
    (assoc this :jetty
           (jetty/run-jetty (-> (myroutes this) hdr/api) {:port port}))))

(defn server [] (map->Server {:port 3002}))


;; =============================================================================
;; App
;; =============================================================================

(def app-components [:server :service :store])

(defrecord SampleApp [config-options store service server]
  component/Lifecycle
  (start [this] (component/start-system this app-components))
  (stop [this] (component/stop-system this app-components)))

(defn app [config-options]
  (let [{:keys [host port]} config-options]
    (map->SampleApp
      {:config-options config-options
       :store (store host port)
       :service (component/using (service config-options) {:store  :store})
       :server (component/using (server) [:service])})))


;; =============================================================================
;; Application entry point
;; =============================================================================

(defn -main [& args]
  (println "application entry point")
  (let [system
        (component/start (app {:host "myhost.com" :port 123}))]))
