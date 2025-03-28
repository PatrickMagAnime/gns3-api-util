#!/bin/bash

_gns3_api_util_completions()
{
    COMPREPLY=($(compgen -W "$(gns3-api-util --help | grep -Eo '^\s*--[a-zA-Z0-9_-]+' | sed 's/^ *//')" -- "${COMP_WORDS[1]}"))
}

complete -F _gns3_api_util_completions gns3-api-util
