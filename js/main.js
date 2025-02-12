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
import React from 'react'
import ReactDOM from 'react-dom'
import ReactDOMClient from 'react-dom/client'
import * as ReactQuery from '@tanstack/react-query'
import * as SRC from 'synapse-react-client'
import './mui.js'

// Append to global scope so these libraries can be accessed in JsInterop classes

self.React = React
self.ReactDOM = ReactDOM
self.ReactDOMClient = ReactDOMClient

self.ReactQuery = ReactQuery

self.SRC = SRC
