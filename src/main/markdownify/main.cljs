(ns markdownify.main
  (:require [reagent.dom :as rd]
            [reagent.core :as r]
            ["showdown" :as showdown]))

(defonce markdown (r/atom ""))
(defonce html     (r/atom ""))
(defonce showdown-converter (showdown/Converter.))
(defonce flash-message (r/atom nil))
(defonce flash-timeout (r/atom nil))

(defn flash
  ([text]
   (flash text 3000))
  ([text ms]
   (js/clearTimeout @flash-timeout)
   (reset! flash-message text)
   (reset! flash-timeout
           (js/setTimeout #(reset! flash-message nil) ms))))

(defn md->html [md]
  (.makeHtml showdown-converter md))

(defn html->md [html]
  (.makeMarkdown showdown-converter html))

(defn copy-to-clipboard [s]
  (let [el (.createElement js/document "textarea")
        selected (when (pos? (-> js/document .getSelection .-rangeCount))
                   (-> js/document .getSelection (.getRangeAt 0)))]
    (set! (.-value el) s)
    (.setAttribute el "readonly" "")
    (set! (-> el .-style .-position) "absolute")
    (set! (-> el .-style .-left) "-9999px")
    (-> js/document .-body (.appendChild el))
    (.select el)
    (.execCommand js/document "copy")
    (-> js/document .-body (.removeChild el))
    (when selected
      (-> js/document .getSelection .removeAllRanges)
      (-> js/document .getSelection (.addRange selected)))))

(defn app []
  [:div {:style {:position :relative}}
   [:div
    {:style {:position :absolute
             :text-align :center
             :max-width 200
             :padding "1em"
             :background-color "yellow"
             :z-index 100
             :border-radius 10
             :transform (if @flash-message
                          "scaleY(1)"
                          "scaleY(0)")
             :transition "transform 0.2s ease-out"}}
    @flash-message]
   [:h1 "Markdownify"]
   [:div
    {:style {:display :flex :textAlign "center"}}
    [:div
     {:style {:flex "1"}}
     [:h2 "Markdown"]
     [:textarea
      {:on-change (fn [e]
                    (reset! markdown (-> e .-target .-value))
                    (reset! html (md->html (-> e .-target .-value))))
       :value @markdown
       :style {:resize "none"
               :width "100%"}
       :rows "10"}]
     [:button
      {:style {:flex "1"}
       :onClick (fn []
                  (copy-to-clipboard @markdown)
                  (flash "Markdown copied to clipboard"))}
      "Copy Markdown"]]
    [:div
     {:style {:flex "1"}}
     [:h2 "HTML"]
     [:textarea
      {:on-change (fn [e]
                    (reset! markdown (html->md (-> e .-target .-value)))
                    (reset! html (md->html (-> e .-target .-value))))
       :value @html
       :style {:resize "none"
               :width "100%"}
       :rows "10"}]
     [:button
      {:style {:flex "1"}
       :onClick (fn []
                  (copy-to-clipboard @html)
                  (flash "HTML copied to clipboard"))}
      "Copy HTML"]]

    [:div
     {:style {:flex "1"}}
     [:h2 "HTML Preview"]
     [:div
      {:dangerouslySetInnerHTML {:__html @html}}]]]])





(defn mount! []
  (rd/render
   [app]
   (.getElementById js/document "app")))

(defn main! []
  (mount!))

(defn reload! []
  (mount!))
