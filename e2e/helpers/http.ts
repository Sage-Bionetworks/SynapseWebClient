import { Page } from '@playwright/test'
import { waitForSrcEndpointConfig } from './utils'

export enum BackendDestinationEnum {
  REPO_ENDPOINT,
  PORTAL_ENDPOINT,
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
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access, @typescript-eslint/no-unsafe-assignment
      const srcEndpoint = await SRC.SynapseEnums.BackendDestinationEnum[
        endpoint
      ]
      // @ts-expect-error: Cannot find name 'SRC'
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access, @typescript-eslint/no-unsafe-call
      return (await SRC.HttpClient.doPost(
        url,
        requestJsonObject,
        accessToken,
        srcEndpoint,
        additionalOptions,
      )) as T
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
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access, @typescript-eslint/no-unsafe-assignment
      const srcEndpoint = await SRC.SynapseEnums.BackendDestinationEnum[
        endpoint
      ]
      // @ts-expect-error: Cannot find name 'SRC'
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access, @typescript-eslint/no-unsafe-call
      return (await SRC.HttpClient.doGet(
        url,
        accessToken,
        srcEndpoint,
        additionalOptions,
      )) as T
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
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access, @typescript-eslint/no-unsafe-assignment
      const srcEndpoint = await SRC.SynapseEnums.BackendDestinationEnum[
        endpoint
      ]
      // @ts-expect-error: Cannot find name 'SRC'
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access, @typescript-eslint/no-unsafe-call
      return (await SRC.HttpClient.doDelete(
        url,
        accessToken,
        srcEndpoint,
        additionalOptions,
      )) as T
    },
    { url, accessToken, endpoint, additionalOptions },
  )
  return response
}

export async function doPut<T>(
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
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access, @typescript-eslint/no-unsafe-assignment
      const srcEndpoint = await SRC.SynapseEnums.BackendDestinationEnum[
        endpoint
      ]
      // @ts-expect-error: Cannot find name 'SRC'
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access, @typescript-eslint/no-unsafe-call
      return (await SRC.HttpClient.doPut(
        url,
        requestJsonObject,
        accessToken,
        srcEndpoint,
        additionalOptions,
      )) as T
    },
    { url, requestJsonObject, accessToken, endpoint, additionalOptions },
  )
  return response
}
