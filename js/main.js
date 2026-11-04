/**
 * Synapse Web Client JavaScript modules
 *
 * JavaScript dependencies that should be loaded to the global scope in SWC can be added here.
 *
 * This file will be bundled and loaded in the SWC application. Excess imports should be avoided to keep this
 * bundle as small as possible.
 */
// vite/modulepreload-polyfill should be included per https://vite.dev/guide/backend-integration
import 'vite/modulepreload-polyfill'
// Install lazy-module load/link error diagnostics before anything else loads, so the
// listeners are registered before any dynamically imported chunk can fail.
import './moduleLoadDiagnostics.js'
import React from 'react'
import ReactDOM from 'react-dom'
import ReactDOMClient from 'react-dom/client'
import * as ReactQuery from '@tanstack/react-query'
import * as SRC from 'synapse-react-client/SWC.index'
import plotlyFactory from 'react-plotly.js/factory'
import './mui.js'
import './markdown.js'
import './globalContext.js'
// Import SWC styles. Vite will compile and handle SCSS now.
import '../src/main/webapp/sass/swc.scss'

// Append to global scope so these libraries can be accessed in JsInterop classes

window.React = React
window.ReactDOM = ReactDOM
window.ReactDOMClient = ReactDOMClient

window.ReactQuery = ReactQuery

window.SRC = SRC

// react-plotly.js/factory is CJS; with Node-style interop the default import is `exports`
window.createPlotlyComponent = plotlyFactory.default
