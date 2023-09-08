import { Page, expect } from '@playwright/test'
import { navigateToHomepageIfPageHasNotBeenLoaded } from './utils'

export enum BackendDestinationEnum {
  REPO_ENDPOINT,
  PORTAL_ENDPOINT,
}

export async function waitForSrcEndpointConfig(page: Page) {
  // window only available after page has initially loaded
  await navigateToHomepageIfPageHasNotBeenLoaded(page)
  // ensure that endpoint config is set,
  // ...so API calls point to the correct stack
  await expect(async () => {
    const response = await page.evaluate('window.SRC.OVERRIDE_ENDPOINT_CONFIG')
    expect(response).not.toBeUndefined()
  }).toPass()
}

export async function doPost<T>(
  page: Page,
  url: string,
  requestJsonObject: unknown,
  accessToken: string | undefined,
  endpoint: BackendDestinationEnum,
  additionalOptions: RequestInit = {},
) {
  await waitForSrcEndpointConfig(page)
  const response = await page.evaluate(
    async ({
      url,
      requestJsonObject,
      accessToken,
      endpoint,
      additionalOptions,
    }) => {
      // @ts-expect-error: Cannot find name 'SRC'
      const srcEndpoint = await SRC.SynapseEnums.BackendDestinationEnum[
        endpoint
      ]
      // @ts-expect-error: Cannot find name 'SRC'
      return await SRC.HttpClient.doPost(
        url,
        requestJsonObject,
        accessToken,
        srcEndpoint,
        additionalOptions,
      )
    },
    { url, requestJsonObject, accessToken, endpoint, additionalOptions },
  )
  return response
}

export async function doGet<T>(
  page: Page,
  url: string,
  accessToken: string | undefined,
  endpoint: BackendDestinationEnum,
  additionalOptions: RequestInit = {},
) {
  await waitForSrcEndpointConfig(page)
  const response = await page.evaluate(
    async ({ url, accessToken, endpoint, additionalOptions }) => {
      // @ts-expect-error: Cannot find name 'SRC'
      const srcEndpoint = await SRC.SynapseEnums.BackendDestinationEnum[
        endpoint
      ]
      // @ts-expect-error: Cannot find name 'SRC'
      return await SRC.HttpClient.doGet(
        url,
        accessToken,
        srcEndpoint,
        additionalOptions,
      )
    },
    { url, accessToken, endpoint, additionalOptions },
  )
  return response
}

export async function doDelete<T>(
  page: Page,
  url: string,
  accessToken: string | undefined,
  endpoint: BackendDestinationEnum,
  additionalOptions: RequestInit = {},
) {
  await waitForSrcEndpointConfig(page)
  const response = await page.evaluate(
    async ({ url, accessToken, endpoint, additionalOptions }) => {
      // @ts-expect-error: Cannot find name 'SRC'
      const srcEndpoint = await SRC.SynapseEnums.BackendDestinationEnum[
        endpoint
      ]
      // @ts-expect-error: Cannot find name 'SRC'
      return await SRC.HttpClient.doDelete(
        url,
        accessToken,
        srcEndpoint,
        additionalOptions,
      )
    },
    { url, accessToken, endpoint, additionalOptions },
  )
  return response
}
