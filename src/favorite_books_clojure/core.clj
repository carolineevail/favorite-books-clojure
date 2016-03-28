(ns favorite-books-clojure.core
  (:require [compojure.core :as c]
            [ring.adapter.jetty :as j]
            [ring.middleware.params :as p]
            [ring.util.response :as r]
            [hiccup.core :as h]
            [clojure.walk :as walk])
  (:gen-class))

(defonce books (atom []))
(defonce server (atom nil))

(add-watch books :save-to-disk
  (fn [_ _ _ _]
    (spit "books.edn" (pr-str @books))))

(c/defroutes app
  (c/GET "/" request
    (h/html [:html
             [:body
              [:form {:action "/add-book" :method "post"}
               [:input {:type "text" :placeholder "Title" :name "title"}]
               [:input {:type "text" :placeholder "Author" :name "author"}]
               [:input {:type "text" :placeholder "Publish year" :name "year"}]
               [:button {:type "submit"} "Add Book"]]
              [:ol
               (map (fn [book]
                      [:li (str(:title book)" "(:author book)" "(:year book))])
                 @books)]]]))
  (c/POST "/add-book" request
    (let [book (walk/keywordize-keys (:params request))]
;         [title (get (:params request) "title")]
;         [author (get (:params request) "author")]
;         [year (get (:params request) "year")]
      (swap! books conj book)
      (r/redirect "/"))))

(defn -main []
  (try
    (let [books-str (slurp "books.edn")
          books-vec (read-string books-str)]
      (reset! books books-vec))
    (catch Exception _))
  (when @server
    (.stop @server))
  (reset! server (j/run-jetty (p/wrap-params app) {:port 3000 :join? false})))
