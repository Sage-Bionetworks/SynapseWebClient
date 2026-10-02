import { atom, createStore, useAtomValue } from 'jotai'
import { createElement } from 'react'
import {
  SynapseContext,
  SynapseSessionManager,
} from 'synapse-react-client/SWC.index'

/* Singleton session manager */
const sessionManager = new SynapseSessionManager()
window.SynapseSessionManager = sessionManager

/* A store that will be used across all React elements in the app */
const contextStore = createStore()

/*
 * Atom that can store the props for SynapseContextProvider.
 * Seeded with a valid "not yet initialized" applicationSessionContext so that any React
 * component mounted (via GWT interop) before window.ContextUtils.setGlobalContext() is first
 * called still has a defined context to read, instead of crashing useApplicationSessionContext().
 */
const contextProviderPropsAtom = atom({
  applicationSessionContext: {
    isAuthenticated: false,
    hasInitializedSession: false,
    isLoadingSSO: false,
    refreshSession: async () => {},
    clearSession: async () => {},
  },
})

/* Wraps children in a FullContextProvider, reading the global context store to configure the context */
function SynapseContextProviderFromStore({ children }) {
  const context = useAtomValue(contextProviderPropsAtom, {
    store: contextStore,
  })

  return createElement(SynapseContext.FullContextProvider, context, children)
}

window.ContextUtils = {
  setGlobalContext: ctx => contextStore.set(contextProviderPropsAtom, ctx),
  SynapseContextProviderFromStore: SynapseContextProviderFromStore,
}
