import { v4 as uuidv4 } from 'uuid'
import {
  USER_STORAGE_STATE,
  USER_VALIDATED_STORAGE_STATE,
} from '../../playwright.config'
import { TestUser } from './types'

const userNamePrefix = 'swc-e2e-user'
export const USER_NAME_LOCALSTORAGE_KEY = 'USER_NAME'

const userValidatedPrefix = 'swc-e2e-user-validated'
export const USER_VALIDATED_NAME_LOCALSTORAGE_KEY = 'USER_VALIDATED_NAME'

const generateUserName = (prefix: string) => {
  // uncomment to use static username for troubleshooting:
  // return prefix

  return `${prefix}-${uuidv4()}`
}
const generateUserPassword = () => {
  // uncomment to use static password for troubleshooting:
  // (after setting USER_PASSWORD in the .env file)
  // return process.env.USER_PASSWORD!

  return `password-${uuidv4()}`
}
const generateUserEmail = (prefix: string) => {
  return `${prefix}-${uuidv4()}@test.com`
}

type UserConfig = {
  // used to ensure that dynamically generated tests have static names
  // ...see https://github.com/microsoft/playwright/issues/24273#issuecomment-1640003150
  testName: string
  storageStatePath: string
  localStorageKey: string
  user: TestUser
}

export const userConfigs: UserConfig[] = [
  {
    testName: userNamePrefix,
    storageStatePath: USER_STORAGE_STATE,
    localStorageKey: USER_NAME_LOCALSTORAGE_KEY,
    user: {
      username: generateUserName(userNamePrefix),
      email: generateUserEmail(userNamePrefix),
      password: generateUserPassword(),
      tou: true,
      validatedUser: false,
    },
  },
  {
    testName: userValidatedPrefix,
    storageStatePath: USER_VALIDATED_STORAGE_STATE,
    localStorageKey: USER_VALIDATED_NAME_LOCALSTORAGE_KEY,
    user: {
      username: generateUserName(userValidatedPrefix),
      email: generateUserEmail(userValidatedPrefix),
      password: generateUserPassword(),
      tou: true,
      validatedUser: true,
    },
  },
]
