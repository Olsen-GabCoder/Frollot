(function (config) {
    if (config.output && config.output.webassemblyModuleFilename) {
        config.output.webassemblyModuleFilename = 'composeApp.wasm';
    }
})(config);