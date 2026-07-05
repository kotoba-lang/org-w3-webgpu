(ns w3.webgpu
  "Raw W3C WebGPU JS API — thin ClojureScript wrapper, one function per spec
   call. No render-graph, no EDN vocabulary, no scene/material opinions: this
   is the binding layer other repos (kami-webgpu) build on, the same way
   org-khronos-glb/org-materialx/org-openusd are the raw-spec layer under
   kami-engine's own EDN domain vocabulary.

   Descriptors (desc/opts args below) are plain JS objects (`#js {...}`),
   built by the caller with the spec's own camelCase keys — this wrapper does
   not translate Clojure maps into descriptors, since WebGPU's camelCase,
   multi-word keys (depthWriteEnabled, shaderLocation, colorAttachments, ...)
   don't round-trip through a kebab-case Clojure map without a translation
   layer this repo deliberately doesn't own. kami-webgpu's render-IR/EDN is
   that layer, one level up.

   https://www.w3.org/TR/webgpu/"
  (:require [clojure.string :as str]))

(defn gpu
  "js/navigator.gpu, or nil if the browser has no WebGPU implementation."
  []
  (.-gpu js/navigator))

(defn supported? []
  (some? (gpu)))

(defn request-adapter!
  "-> Promise<GPUAdapter|nil>. opts, if given, is a GPURequestAdapterOptions JS object."
  ([] (request-adapter! nil))
  ([opts] (.requestAdapter (gpu) opts)))

(defn request-device!
  "-> Promise<GPUDevice>. opts, if given, is a GPUDeviceDescriptor JS object."
  ([adapter] (request-device! adapter nil))
  ([adapter opts] (.requestDevice adapter opts)))

(defn preferred-canvas-format []
  (.getPreferredCanvasFormat (gpu)))

(defn get-context
  "canvas.getContext(\"webgpu\") -> GPUCanvasContext, or nil."
  [canvas]
  (.getContext canvas "webgpu"))

(defn configure-context!
  "ctx.configure(desc) — desc is a GPUCanvasConfiguration JS object
   (must include :device/:format, spelled `device`/`format` as JS keys)."
  [ctx desc]
  (.configure ctx desc))

(defn- ->CONST [flag]
  (-> flag name (str/replace "-" "_") str/upper-case))

(defn buffer-usage
  "js/GPUBufferUsage.<FLAG>, e.g. (buffer-usage :copy-dst) -> GPUBufferUsage.COPY_DST."
  [flag]
  (aget js/GPUBufferUsage (->CONST flag)))

(defn texture-usage
  "js/GPUTextureUsage.<FLAG>, e.g. (texture-usage :render-attachment)."
  [flag]
  (aget js/GPUTextureUsage (->CONST flag)))

(defn create-buffer!
  "device.createBuffer(desc), desc: GPUBufferDescriptor JS object."
  [device desc]
  (.createBuffer device desc))

(defn write-buffer!
  "queue.writeBuffer(buffer, offset, data). data must already be a typed array."
  ([queue buffer data] (write-buffer! queue buffer 0 data))
  ([queue buffer offset data] (.writeBuffer queue buffer offset data)))

(defn create-texture!
  "device.createTexture(desc), desc: GPUTextureDescriptor JS object."
  [device desc]
  (.createTexture device desc))

(defn create-view
  "texture.createView(desc?), desc: GPUTextureViewDescriptor JS object."
  ([texture] (create-view texture nil))
  ([texture desc] (.createView texture desc)))

(defn create-sampler!
  [device desc]
  (.createSampler device desc))

(defn create-shader-module!
  "device.createShaderModule(desc), desc: #js {:code <wgsl-string>}."
  [device desc]
  (.createShaderModule device desc))

(defn create-render-pipeline!
  [device desc]
  (.createRenderPipeline device desc))

(defn get-bind-group-layout
  [pipeline index]
  (.getBindGroupLayout pipeline index))

(defn create-bind-group!
  [device desc]
  (.createBindGroup device desc))

(defn create-command-encoder!
  ([device] (create-command-encoder! device nil))
  ([device desc] (.createCommandEncoder device desc)))

(defn begin-render-pass!
  [encoder desc]
  (.beginRenderPass encoder desc))

(defn set-pipeline! [pass pipeline] (.setPipeline pass pipeline))
(defn set-bind-group! [pass index bind-group] (.setBindGroup pass index bind-group))
(defn set-vertex-buffer!
  ([pass slot buffer] (.setVertexBuffer pass slot buffer))
  ([pass slot buffer offset] (.setVertexBuffer pass slot buffer offset)))
(defn set-index-buffer!
  [pass buffer format]
  (.setIndexBuffer pass buffer format))
(defn draw-indexed!
  ([pass index-count] (.drawIndexed pass index-count))
  ([pass index-count instance-count] (.drawIndexed pass index-count instance-count))
  ([pass index-count instance-count first-index] (.drawIndexed pass index-count instance-count first-index))
  ([pass index-count instance-count first-index base-vertex]
   (.drawIndexed pass index-count instance-count first-index base-vertex))
  ([pass index-count instance-count first-index base-vertex first-instance]
   (.drawIndexed pass index-count instance-count first-index base-vertex first-instance)))
(defn end-pass! [pass] (.end pass))

(defn finish!
  "command-encoder.finish() -> GPUCommandBuffer."
  ([encoder] (.finish encoder))
  ([encoder desc] (.finish encoder desc)))

(defn submit!
  "queue.submit([...command-buffers])."
  [queue command-buffers]
  (.submit queue (into-array command-buffers)))

(defn current-texture
  "ctx.getCurrentTexture()."
  [ctx]
  (.getCurrentTexture ctx))
