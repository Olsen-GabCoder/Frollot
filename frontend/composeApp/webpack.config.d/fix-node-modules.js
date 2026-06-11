config.resolve = config.resolve || {};
config.resolve.fallback = config.resolve.fallback || {};

// Node.js fallbacks
config.resolve.fallback.os = false;
config.resolve.fallback.path = false;
config.resolve.fallback.crypto = false;
config.resolve.fallback.stream = false;
config.resolve.fallback.util = false;
config.resolve.fallback.url = false;
config.resolve.fallback.assert = false;
config.resolve.fallback.constants = false;
config.resolve.fallback.querystring = false;
config.resolve.fallback.http = false;
config.resolve.fallback.https = false;
config.resolve.fallback.zlib = false;
config.resolve.fallback.events = false;
config.resolve.fallback.timers = false;
config.resolve.fallback.tty = false;
config.resolve.fallback.fs = false;
config.resolve.fallback.net = false;
config.resolve.fallback.tls = false;
config.resolve.fallback.child_process = false;
config.resolve.fallback.cluster = false;
config.resolve.fallback.dgram = false;
config.resolve.fallback.dns = false;
config.resolve.fallback.module = false;
config.resolve.fallback.punycode = false;
config.resolve.fallback.readline = false;
config.resolve.fallback.repl = false;
config.resolve.fallback.string_decoder = false;
config.resolve.fallback.vm = false;
config.resolve.fallback.worker_threads = false;
config.resolve.fallback.inspector = false;
config.resolve.fallback.trace_events = false;
config.resolve.fallback.async_hooks = false;
config.resolve.fallback.http2 = false;
config.resolve.fallback.perf_hooks = false;

// WASM and ES6 modules configuration
config.experiments = config.experiments || {};
config.experiments.asyncWebAssembly = true;
config.experiments.layers = true;

// Ensure .mjs files are treated as ES modules
config.resolve.extensions = config.resolve.extensions || [];
if (!config.resolve.extensions.includes('.mjs')) {
    config.resolve.extensions.push('.mjs');
}

config.module = config.module || {};
config.module.rules = config.module.rules || [];

// Rule for .mjs files
config.module.rules.push({
    test: /\.mjs$/,
    type: 'javascript/esm',
    resolve: {
        fullySpecified: false
    }
});

// Completely disable HMR for WASM builds
config.devServer = config.devServer || {};
config.devServer.hot = false;           // Disable hot module replacement
config.devServer.liveReload = true;     // Keep live reload for development
config.devServer.client = config.devServer.client || {};
config.devServer.client.overlay = false; // Disable error overlay that might use HMR

 

// Remove any HotModuleReplacementPlugin if present
if (config.plugins) {
    config.plugins = config.plugins.filter(plugin => {
        return !plugin || !plugin.constructor || plugin.constructor.name !== 'HotModuleReplacementPlugin';
    });
}

// Ensure no HMR runtime is injected
config.optimization = config.optimization || {};
config.optimization.runtimeChunk = false; // Prevent HMR runtime injection
