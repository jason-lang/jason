# List of all commands

You can get help for any command with `--help`. For example:

```
jason mas start --help
```

## MAS level


```
Usage: jason mas (start | stop | list)
commands to handle running Multi-Agent Systems

Sub-commands:
  start  starts a new (empty) MAS
  stop   stops a MAS
  list   lists current running MAS
```
### MAS start

```
Usage: jason mas start [--console] [--no-net] [<mas name>]
starts a new (empty) MAS
      [<mas name>]   MAS unique identification
      --console      output will be sent to the console instead of a GUI
      --no-net       disable all net services (mind inspector, runtime
                       services, Mbeans, ...

```


### MAS stop

```
Usage: jason mas stop [--exit] [--deadline=<deadline>] [<mas name>]
stops a MAS
      [<mas name>]   MAS unique identification
      --deadline=<deadline>
                     the amount of time (in milliseconds) to wait for stopping the MAS
      --exit         stops the MAS and terminates the process
```

## Agent level

```
Usage: jason agent (start | stop | list | run-as | load-into | mind | status)
commands to handle agents

Commands:
  start      starts a new (empty) agent
  stop       kills an agent
  list       lists running agents
  run-as     executes commands for an agent
  mind       inspects the mind of an agent
  status     shows the status of an agent
  load-into  loads some ASL code into a running agent
```


### Agent start

```
Usage: jason agent start [--instances=<instances>] [--mas-name=<mas name>]
                         [--source=<source file>] <agent name>
starts a new (empty) agent
      <agent name>   agent unique identification
      --instances=<instances>
                     how many agents should be created
      --mas-name=<mas name>
                     MAS unique identification
      --source=<source file>
                     file (or URL) for the source code of the agent.
```

The source code can be informed at the  end of the command enclosed by `{` and `}`. For instance:

```
jason agent start bob { +!g <- .print(ok). }
```

### Other agent commands

Type `jason agent stop --help` for details of `stop` command ...

## Application level

```
Usage: jason app (create | compile | add-agent | add-gradle )
commands to handle applications

Commands:
  create      creates the files of a new application
  compile     compiles the java classes of an application
  add-agent   adds a new agent into the application
  add-gradle  adds a Gradle script for the application
```