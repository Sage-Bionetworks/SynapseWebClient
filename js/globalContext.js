import { atom, createStore, Provider, useAtomValue } from 'jotai'
import { createElement } from 'react'
import { SynapseContext } from 'synapse-react-client'

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
