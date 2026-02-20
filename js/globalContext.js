import { atom, createStore, useAtomValue } from 'jotai'
import { createElement } from 'react'
import { SynapseContext, SynapseSessionManager } from 'synapse-react-client'

/* Singleton session manager */
const sessionManager = new SynapseSessionManager()
window.SynapseSessionManager = sessionManager

/* A store that will be used across all React elements in the app */
const contextStore = createStore()

/* Atom that can store the props for SynapseContextProvider */
const contextProviderPropsAtom = atom({})

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
