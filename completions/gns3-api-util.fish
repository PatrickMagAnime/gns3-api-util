function __fish_gns3_api_util_complete
    set -l options (
        gns3-api-util --help | grep -Eo '^\s*--[a-zA-Z0-9_-]+' | sed 's/^ *//'
    )
    for option in $options
        echo $option
    end
end

complete -c gns3-api-util -f -a '(__fish_gns3_api_util_complete)'
