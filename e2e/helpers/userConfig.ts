import { v4 as uuidv4 } from 'uuid'
import { TestUser } from './types'

export const storageStateDir = 'playwright/.auth/'

export const userPrefix = 'swc-e2e-user'
export const userValidatedPrefix = 'swc-e2e-user-validated'
const userPrefixes = [userPrefix, userValidatedPrefix] as const
export type UserPrefix = (typeof userPrefixes)[number]

const generateUserName = (prefix: string) => {
  // uncomment to use static username for troubleshooting:
  // ...and ensure that usernames are unique per worker
  // return `local-${prefix}-${process.env.TEST_PARALLEL_INDEX}`

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
  [key in UserPrefix]: TestUser
}

export const userConfigs: UserConfig = {
  [userPrefix]: {
    username: generateUserName(userPrefix),
    email: generateUserEmail(userPrefix),
    password: generateUserPassword(),
    tou: true,
    validatedUser: false,
  },
  [userValidatedPrefix]: {
    username: generateUserName(userValidatedPrefix),
    email: generateUserEmail(userValidatedPrefix),
    password: generateUserPassword(),
    tou: true,
    validatedUser: true,
  },
}
