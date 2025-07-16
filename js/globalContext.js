import { atom, createStore, Provider, useAtomValue } from 'jotai'
import { createElement } from 'react'
import { SynapseContext } from 'synapse-react-client'

const contextStore = createStore()
const contextAtom = atom({})

function _SynapseContextProviderFromStore({ children }) {
  const context = useAtomValue(contextAtom)

  return createElement(SynapseContext.FullContextProvider, context, children)
}

function SynapseContextProviderFromStore(props) {
  return createElement(
    Provider,
    { store: contextStore },
    createElement(_SynapseContextProviderFromStore, props),
  )
}

window.ContextUtils = {
  setGlobalContext: ctx => contextStore.set(contextAtom, ctx),
  contextStore: contextStore,
  SynapseContextProviderFromStore: SynapseContextProviderFromStore,
}
