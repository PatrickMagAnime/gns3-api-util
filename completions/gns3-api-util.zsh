#compdef gns3-api-util

_arguments "*:: :->args"

_gns3_api_util() {
    local -a options
    options=($(gns3-api-util --help | grep -Eo '^\s*--[a-zA-Z0-9_-]+' | sed 's/^ *//'))

    _describe 'options' options
}

if [[ $state == args ]]; then
    _gns3_api_util
fi
