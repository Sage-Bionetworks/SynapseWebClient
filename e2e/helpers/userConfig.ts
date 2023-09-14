import { v4 as uuidv4 } from 'uuid'
import { TestUser } from './types'

export const storageStateDir = 'playwright/.auth/'

export const userPrefix = 'swc-e2e-user'
export const USER_NAME_LOCALSTORAGE_KEY = 'USER_NAME'

export const userValidatedPrefix = 'swc-e2e-user-validated'
export const USER_VALIDATED_NAME_LOCALSTORAGE_KEY = 'USER_VALIDATED_NAME'

const generateUserName = (prefix: string) => {
  // uncomment to use static username for troubleshooting:
  // ...and ensure that usernames are unique per worker
  // return `${prefix}-${process.env.TEST_PARALLEL_INDEX}`

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
  userPrefix: string
  localStorageKey: string
  user: TestUser
}

export const userConfigs: UserConfig[] = [
  {
    userPrefix: userPrefix,
    localStorageKey: USER_NAME_LOCALSTORAGE_KEY,
    user: {
      username: generateUserName(userPrefix),
      email: generateUserEmail(userPrefix),
      password: generateUserPassword(),
      tou: true,
      validatedUser: false,
    },
  },
  {
    userPrefix: userValidatedPrefix,
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
