# REST Interface

**Base URL:** `/tqm/rest/`

On successful request the HTTP status 200 will be returned.
If the request is not valid another appropriate status code will be returned.

## Client Registering

Required to communicate/receive push messages.

**Resource:** `clients`

**Method:** `POST`

**Parameters:** 

    { 
        name: 'myUser', 
        project: 'projectKey', 
        role: 'SM'|'DEV' 
    } 

## Client Unregistering

**Resource:** `clients/name`

**Method:** `DELETE`

## JIRA Projects
 
**Resource:** `metrics/jira/projects`

**Method:** `GET`

**Headers:** `Authorization: Basic base64encoded(user:pass)`

**Returns:**

    [
        {
            key: 'projectKey',
            name: 'Full name of project'
        },
        ...
    ]


## JIRA Issues
 
**Resource:** `metrics/jira/projects/projectKey/issues`

**Method:** `GET`

**Headers:** `Authorization: Basic base64encoded(user:pass)`

**Returns:**

    [
        {
            key: 'issueKey',
            name: 'Full name of issue',
            assignee: 'username',
            hoursPerUpdate: x.y,     // Hours per effort update (Double)
            updateRateMetricCategory: 'OK'|'WARN'|'CRITICAL',
            link: 'url to issue on JIRA server'
        },
        ...
    ]

## JIRA Users
 
**Resource:** `metrics/jira/projects/projectKey/users`

**Method:** `GET`

**Headers:** `Authorization: Basic base64encoded(user:pass)`

**Returns:**

    [
        {
            name: 'username',
            assignedIssues: [
                {
                    key: 'issueKey',
                    name: 'Full name of issue',
                    assignee: 'username',
                    hoursPerUpdate: x.y,     // Hours per effort update (Double)
                    updateRateMetricCategory: 'OK'|'WARN'|'CRITICAL',
                    link: 'url to issue on JIRA server'
                },
                ...
            ],
            assignedIssuesCount: x,     // Integer
            assignedIssuesCountMetricCategory: 'OK'|'WARN'|'CRITICAL'
        },
        ...
    ]

## Create Communication (Dialog)

**Resource:** `dialogs`

**Method:** `POST`

**Parameters:** 

    { 
        initiator: 'username',
        affected: 'username',
        subject: 'subject',
        violationID: 'violationID'    // Long
    }

**Returns:**

    {
        dialogID: x     // Long
    }
    
    
## Get Dialogs for specific user

Get all dialogs in which the given user is a participant (initiator or affected).

**Resource:** `dialogs/users/userName`

**Method:** `GET`

**Returns:**

    [
        {
            dialogID: x,     // Long
            resolvedInitiator: true|false,
            resolvedAffected: true|false,
            initiator: 'userName',
            affected: 'userName',
            subject: 'subject of dialog',
            violationID: 'violationID',    // Long
            timestamp: x,       // Long
            messages: [
                {
                    user: 'username',
                    timestamp: x,   // Timestamp of creation (Long) Millis since Epoch
                    body: 'body of message'
                },
                ...
            ]
        },
        ...
    ]

    

## Get Communication Messages of Dialog

**Resource:** `dialogs/dialogID`

**Method:** `GET`

**Returns:**

    {
        dialogID: x,     // Long
        resolvedInitiator: true|false,
        resolvedAffected: true|false,
        initiator: 'user',
        affected: 'user',
        subject: 'subject of dialog',
        violationID: 'violationID',    // Long
        timestamp: x,       // Long
        messages: [
            {
                user: 'username',
                timestamp: x,   // Timestamp of creation (Long) Millis since Epoch
                body: 'body of message'
            },
            ...
        ]
    }
    
## Add message to dialog

**Resource:** `dialogs/dialogID`

**Method:** `POST`

**Parameters:** 

    { 
        user: 'username',
        body: 'body of message'
    }
    
## Mark a dialog as resolved by one person

**Resource:** `dialogs/dialogID/resolve`

**Method:** `POST`

**Parameters:** 

    { 
        user: 'username'
    }


# WebSocket Interface

Connect to web socket on `ws://host:port/tqm/ws` 

All communication in both directions is done via JSON messages in the form

    {
        type: 'type',
        content: {}
    }


## Connection Protocol

- Register a user and a project via the REST interface (see section REST). Otherwise the client will not receive push messages.
- Send via web socket a message with type `attach` and content `{ user: 'username', project: 'projectKey' }` (Client has to be registered before)
- The client will now receive push messages from the server for the given username and project whenever it is connected.
- Note: Re-attaching the user to another project means he will be removed from the previous project.
- When losing connection and a reconnect happens all remaining push messages will be delivered.

### Attach message

    {
        type: 'attach',
        content: {
            user: 'username', 
            project: 'projectKey'
        }
    }

### Push Message Types

All push messages will be triggered if a metric is violated and categorized as **CRITICAL**.
They may be re-sent if a user has not reacted to them (i.e. created a dialog or fixed the metric).

**User has violated the remaining effort update rate of issues metric**: 

    {
        type: 'metricViolation',
        content: {
            developer: 'username',
            violationName: 'updateRateViolated',
            violationID: x,     // Long
            communicateWithDeveloper: true|false,
            issue: {
                key: 'issueKey',
                name: 'Full name of issue',
                hoursPerUpdate: x.y     // Hours per effort update (Double)
            }
        }
    }

**User has violated the assigned issues count metric**:
 
    {
        type: 'metricViolation',
        content: {
            developer: 'username',
            violationName: 'assignedIssuesCountViolated',
            violationID: x,     // Long
            communicateWithDeveloper: true|false
            issues: [
                {
                    key: 'issueKey',
                    name: 'Full name of issue'
                },
                ...
            ],
            count: x    // Integer
        }
    }
    
**A dialog has been created and the user is affected:**

    {
        type: 'dialogCreated',
        content: {
            dialogID: x     // Long 
        }
    }
    
    
**A dialog is unresolved and a participant has not responded to a dialog message:**

    {
        type: 'missingDialogResponse',
        content: {
            dialogID: x     // Long 
        }
    }
    
**A message to a dialog has been created and the user is affected:**

    {
        type: 'dialogMessageCreated',
        content: {
            dialogID: x     // Long
        }
    }
    
    
# Roles

A user can have different roles:
 
Both receive a notification if they are involved in a dialog and the dialog partner has created a new message to the dialog. 

## Scrum Master (SM)

Receives a notification if a developer violates a metric and hasn't fixed it in the specified amount of time.
Also receives a notification if he should communicate with a developer (may be the same as the metric violation notification and
is indicated by the field communicateWithDeveloper).
If the communication is not made he receives the same notification again. 

## Developer (DEV)

Receives a notification if he violates a metric. He has a specified amount of time to fix the metric.
He receives a notification if a communication with him was initiated.
He receives a reminder notification if he didn't respond to a communication after a specified amount of time.


