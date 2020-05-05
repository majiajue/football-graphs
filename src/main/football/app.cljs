(ns football.app
  (:require
   [utils.core :refer [assoc-pos
                       set-canvas-dimensions
                       canvas-dimensions
                       mobile-mapping
                       hash-by]]
   [utils.dom :refer [plot-matches-list
                      reset-dom
                      slide-graph
                      loader-element
                      is-mobile?
                      fix-nav
                      scroll-top
                      set-collapse
                      fix-back
                      toogle-theme-btn
                      plot-dom
                      toogle-theme
                      fetch-file
                      dom]]

   [football.observables :refer [select-metrics$
                                 sticky-nav$
                                 slider$]]
   [football.matches :refer [matches-files-hash]]
   [football.store :refer [store update-store]]
   [football.config :refer [config]]
   [football.draw-graph :refer [force-graph]]))

(set! *warn-on-infer* true)

(defn all-canvas
  [{:keys [scale]}]
  (-> js/document
      (.querySelectorAll ".graph__canvas")
      array-seq
      (#(map (fn [el]
               {:id (.getAttribute el "id")
                :data (-> el
                          (.getAttribute "data-match-id")
                          keyword
                          ((fn [k] (-> @store (get-in [k]))))
                          ((fn [v]
                             (let [id (-> el (.getAttribute "data-team-id") keyword)
                                   orientation (-> el
                                                   (.getAttribute "data-orientation")
                                                   keyword
                                                   ((fn [k] (if (is-mobile?) (mobile-mapping k) k))))
                                   nodes (-> v :nodes id (assoc-pos el orientation))]
                               (((set-canvas-dimensions scale) orientation) el)
                               {:match-id (-> v :match-id)
                                :nodes nodes
                                :canvas-dimensions (canvas-dimensions scale)
                                :orientation orientation
                                :nodeshash (-> nodes
                                               ((fn [n]
                                                  (reduce (partial hash-by :id) (sorted-map) n))))
                                :links (-> v :links id)
                                :min-max-values (-> v :min-max-values)
                                :label (-> v :label)}))))}) %))))

(defn plot-graphs
  "Plot all data inside canvas."
  [{:keys [node-radius-metric
           node-color-metric
           name-position
           scale
           mobile?
           min-passes-to-display
           theme-background
           theme-lines-color
           theme-font-color
           theme-edge-color-range
           theme-node-color-range
           theme-outline-node-color]}]
  (doseq [canvas (all-canvas {:scale scale})]
    (force-graph {:data (-> (merge (-> canvas :data)
                                   {:graphs-options
                                    {:min-passes-to-display min-passes-to-display}
                                    :field
                                    {:background theme-background
                                     :lines-color theme-lines-color
                                     :lines-width 2}}) clj->js)
                  :config (config {:id (canvas :id)
                                   :node-radius-metric node-radius-metric
                                   :node-color-metric node-color-metric
                                   :outline-node-color theme-outline-node-color
                                   :name-position name-position
                                   :font-color theme-font-color
                                   :mobile? mobile?
                                   :node-color-range theme-node-color-range
                                   :edge-color-range theme-edge-color-range
                                   :min-max-values
                                   (-> canvas :data :min-max-values)})})))

(defn init
  "Init graph interations."
  []
  (let [metrics (select-metrics$)
        input$ (-> metrics :input$)
        click$ (-> metrics :click$)
        list$ (-> metrics :list$)
        opts {:mobile? (is-mobile?)
              :scale 9
              :name-position :bottom}]
    (do
      (reset-dom)
      (plot-matches-list (->> matches-files-hash vals (sort-by :label)))
      (sticky-nav$)
      (slider$)
      (-> list$
          (.subscribe (fn [obj]
                        (do
                          (slide-graph (-> obj :select-match name))
                          (fix-back 1)
                          (fix-nav 1)
                          (scroll-top)
                          (set-collapse (-> dom :slider-home) 1)
                          (set-collapse (-> dom :slider-graph) 0)
                          (-> matches-files-hash
                              (get-in [(-> obj :select-match)])
                              ((fn [{:keys [filename match-id]}]
                                 (let [store-data (get-in @store [(-> match-id str keyword)])]
                                   (if store-data
                                     (-> store-data
                                         vector
                                         ((fn [d]
                                            (do
                                              (plot-dom d)
                                              (-> obj (merge opts) plot-graphs)))))
                                     (do
                                       (-> dom :plot-section (#(set! (.-innerHTML %) loader-element)))
                                       (fetch-file
                                        filename
                                        [update-store
                                         (fn [d] (-> d vector plot-dom))
                                         (fn [] (-> obj (merge opts) plot-graphs))])))))))))))

      (-> input$
          (.subscribe #(-> % (merge opts) plot-graphs)))
      (-> click$
          (.subscribe #(-> % (merge opts)
                           ((fn [{:keys [theme-text theme] :as obj}]
                              (do
                                (toogle-theme-btn theme-text)
                                (toogle-theme theme)
                                (plot-graphs obj))))))))))

(defn reload! [] (init))
