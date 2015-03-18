## Setup

1. Create a parse.com account and get the `application id` and `client key`.
2. Sign up for an account at mailgun.com
3. In Parse config, set the following values:
 * notifications_to
 * notifications_from
 * mailgun_domain
 * mailgun_apikey

4. Create `secrets.properties` in the root of the project before building and add the following:

  ```
  parse.applicationId=...
  parse.clientKey=....
  ```
