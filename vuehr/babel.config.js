module.exports = {
    presets: [
        '@vue/app',
        [
            '@babel/preset-env', {
            modules: false  // 或者 'commonjs'
        }]
    ],
    plugins: [
        [
            "component",
            {
                "libraryName": "element-ui",
                "styleLibraryName": "theme-chalk"
            }
        ]
    ]
}
