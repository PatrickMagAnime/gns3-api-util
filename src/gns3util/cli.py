import click
from .auth import auth
from .get import get
from .post import post
from .put import put
from .delete import delete


@click.group()
@click.option('--server', '-s', required=True, type=str, help="GNS3 server URL")
@click.pass_context
def gns3util(ctx, server):
    """A utility for GNS3."""
    ctx.ensure_object(dict)
    ctx.obj = {'server': server}


gns3util.add_command(auth)
gns3util.add_command(get)
gns3util.add_command(post)
gns3util.add_command(put)
gns3util.add_command(delete)

if __name__ == '__main__':
    gns3util()
