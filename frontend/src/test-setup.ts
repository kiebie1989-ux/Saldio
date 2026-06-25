// Globale Test-Polyfills (jsdom-Umgebung kennt ResizeObserver nicht; ECharts benötigt ihn).
class ResizeObserverStub {
  observe(): void {}
  unobserve(): void {}
  disconnect(): void {}
}

if (!('ResizeObserver' in globalThis)) {
  (globalThis as unknown as { ResizeObserver: unknown }).ResizeObserver = ResizeObserverStub;
}

// ECharts/zrender planen ihren Animations-Loop über requestAnimationFrame. In jsdom feuert dieser
// nach Test-Teardown und erzeugt einen unbehandelten Fehler. In Tests wird keine Animation benötigt
// -> rAF neutralisieren, damit der Loop nicht weiterläuft.
(globalThis as unknown as { requestAnimationFrame: (cb: FrameRequestCallback) => number })
  .requestAnimationFrame = () => 0;
(globalThis as unknown as { cancelAnimationFrame: (id: number) => void }).cancelAnimationFrame =
  () => {};

