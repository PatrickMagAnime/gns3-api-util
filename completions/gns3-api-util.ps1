Register-ArgumentCompleter -Native -CommandName gns3-api-util -ScriptBlock {
    param($commandName, $wordToComplete, $cursorPosition)
    $options = gns3-api-util --help | Select-String -Pattern '^\s*--[a-zA-Z0-9_-]+' | ForEach-Object { $_.Matches.Value.Trim() }
    $options | Where-Object { $_ -like "$wordToComplete*" } | ForEach-Object { [System.Management.Automation.CompletionResult]::new($_, $_, 'ParameterName', $_) }
}
